/*
 * Copyright 2018 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package stroom.pipeline.reader;

import stroom.pipeline.LocationFactory;
import stroom.pipeline.errorhandler.ErrorReceiver;
import stroom.util.shared.Location;
import stroom.util.shared.Severity;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FindReplaceFilter extends FilterReader {
    private static final int MIN_SIZE = 1000;

    private final Pattern pattern;
    private final String replacement;
    private final int maxReplacements;
    private final int bufferSize;
    private final LocationFactory locationFactory;
    private final ErrorReceiver errorReceiver;
    private final String elementId;

    private int inputOffset;
    private int replacementCount;
    private int totalReplacementCount;
    private final InBuffer inBuffer;
    private final OutBuffer outBuffer = new OutBuffer();
    private final StringBuffer replacementBuffer = new StringBuffer();

    private int lineNo;
    private int colNo;
    private boolean allowReplacement;

    private FindReplaceFilter(final Reader reader,
                              final String find,
                              final String replacement,
                              final Integer maxReplacements,
                              final boolean regex,
                              final boolean dotAll,
                              final int bufferSize,
                              final LocationFactory locationFactory,
                              final ErrorReceiver errorReceiver,
                              final String elementId) {
        super(reader);
        this.bufferSize = Math.max(MIN_SIZE, bufferSize);
        this.locationFactory = locationFactory;
        this.errorReceiver = errorReceiver;
        this.elementId = elementId;

        final int doubleBufferSize = 2 * this.bufferSize;
        inBuffer = new InBuffer(reader, MIN_SIZE, doubleBufferSize);

        if (find == null) {
            this.pattern = null;
            this.replacement = null;
            this.maxReplacements = 0;

        } else {
            this.maxReplacements = maxReplacements;

            if (regex) {
                if (dotAll) {
                    this.pattern = Pattern.compile(find, Pattern.DOTALL);
                } else {
                    this.pattern = Pattern.compile(find);
                }
                this.replacement = replacement;
            } else {
                this.pattern = Pattern.compile(find, Pattern.LITERAL);
                this.replacement = Matcher.quoteReplacement(replacement);
            }
        }

        clear();
    }

    /**
     * Read text into the input buffer, replace the first match if possible and copy replaced text to the output buffer.
     */
    private void performReplacement() throws IOException {
        inBuffer.fillBuffer();

        boolean doneReplacement = false;
        final CharSequence charSequence = new PaddingWrapper(inBuffer, inputOffset != 0);
        final Matcher matcher = pattern.matcher(charSequence);

        if (matcher.find(inputOffset)) {
            final int start = matcher.start();
            final int end = matcher.end();

            // Guard against matching up to the end of the visible buffer unless we have reached the end of the file.
            if (start <= bufferSize || inBuffer.isEof()) {
                final boolean matchedBufferStart = start == 0;
                final boolean matchedBufferEnd = end == charSequence.length() && !inBuffer.isEof();

                if (matchedBufferStart && matchedBufferEnd) {
                    // If we matched all text in the buffer but aren't actually at the start of the stream or at the end then this is not likely to be correct.
                    error("The pattern matched all text in the buffer. Consider changing your match expression or making the buffer bigger.");
                } else if (matchedBufferEnd) {
                    if (!inBuffer.isEof()) {
                        // If we matched to the end of the buffer and we are not at the end of the stream then this is not likely to be correct.
                        error("The pattern matched text at the end of the buffer when we are not at the end of the stream. Consider changing your match expression or making the buffer bigger");
                    }
                } else {
                    // Append the replacement. Note that we do this with a separate buffer as we don't actually want to keep the start of the string that occurs before the `lastPosition` that is added by appendReplacement. We are using `lastPosition` in our matches so that matches after the first do not match a start anchor.
                    matcher.appendReplacement(replacementBuffer, replacement);
                    outBuffer.append(replacementBuffer, inputOffset, replacementBuffer.length());
                    replacementBuffer.setLength(0);

                    // Move to the end of the match minus one char.
                    final int advance = end - inputOffset;
                    if (advance > 0) {
                        move(Math.min(inBuffer.length(), advance));
                    } else {
                        // Copy a single char to at least advance rather than getting stuck at this position. This mimics the behaviour of`replaceAll()`.
                        copyBuffer(Math.min(inBuffer.length(), 1));
                    }

                    doneReplacement = true;
                    replacementCount++;
                    totalReplacementCount++;

                    // Stop any further replacements if we got to our max replacement count or didn't advance and are at the end of the input.
                    if (replacementCount == maxReplacements || (advance == 0 && inBuffer.isEof() && inBuffer.length() == 0)) {
                        allowReplacement = false;
                    }
                }
            }
        }

        if (!doneReplacement) {
            // We didn't manage to replace anything then copy the buffered text to the output.
            if (inBuffer.isEof()) {
                // Copy all remaining text to the output buffer.
                copyBuffer(inBuffer.length());
            } else {
                // Copy the current buffer text up to a maximum length of buffer size.
                copyBuffer(Math.min(inBuffer.length(), bufferSize));
            }
        }

        // Subsequent matches will work on padded input so that `^` anchored patterns will no longer match.
        inputOffset = 1;
    }

    private void copyBuffer(int length) {
        if (length > 0) {
            final CharSequence charSequence = inBuffer.subSequence(0, length);
            outBuffer.append(charSequence, 0, charSequence.length());
            move(charSequence.length());
        }
    }

    private void move(int length) {
        if (length > 0) {
            // Move our location to aid error reporting.
            for (int i = 0; i < length; i++) {
                if (inBuffer.charAt(i) == '\n') {
                    lineNo++;
                    colNo = 0;
                } else {
                    colNo++;
                }
            }

            inBuffer.move(length);
        }
    }

    @Override
    public int read(final char[] cbuf, final int offset, final int length) throws IOException {
        if (outBuffer.length() == 0) {
            if (allowReplacement) {
                // Read text into the input buffer, replace the first match if possible and copy replaced text to the output buffer.
                performReplacement();

            } else {
                // Put any remaining text from the input buffer into the output buffer.
                if (inBuffer.length() > 0) {
                    copyBuffer(inBuffer.length());

                } else {
                    // There is no more text to be replaced so pass through.
                    return super.read(cbuf, offset, length);
                }
            }
        }

        // Copy text from the output buffer to the supplied char array up to the requested length or length of the output buffer.
        int len = Math.min(length, outBuffer.length());
        outBuffer.getChars(0, len, cbuf, offset);

        // If there was nothing left in the output buffer, we didn't return any content and we are EOF then return -1.
        if (len == 0 && inBuffer.isEof()) {
            len = -1;
        } else {
            // Move the offset in the output buffer ready for the next read operation.
            outBuffer.move(len);
        }

        return len;
    }

    @Override
    public int read() throws IOException {
        if (outBuffer.length() == 0) {
            if (allowReplacement) {
                // Read text into the input buffer, replace the first match if possible and copy replaced text to the output buffer.
                performReplacement();

            } else {
                // Put any remaining text from the input buffer into the output buffer.
                if (inBuffer.length() > 0) {
                    copyBuffer(inBuffer.length());

                } else {
                    // There is no more text to be replaced so pass through.
                    return super.read();
                }
            }
        }

        // Copy text from the output buffer to the supplied char array up to the requested length or length of the output buffer.
        final int len = Math.min(1, outBuffer.length());
        int result = -1;
        if (len > 0) {
            result = outBuffer.charAt(0);
            // Move the offset in the output buffer ready for the next read operation.
            outBuffer.move(len);
        }

        return result;
    }

    int getTotalReplacementCount() {
        return totalReplacementCount;
    }

    private void error(final String error) {
        // Allow no further replacements for this input.
        allowReplacement = false;

        // Log the error.
        if (errorReceiver != null) {
            Location location = null;
            if (locationFactory != null) {
                location = locationFactory.create(lineNo, colNo);
            }
            errorReceiver.log(Severity.ERROR, location, elementId, error, null);
        }
    }

    void clear() {
        // Be ready to start matching from the start again.
        inputOffset = 0;
        // Reset the replacement count.
        replacementCount = 0;
        // Clear out buffer.
        outBuffer.clear();
        // Reset line number.
        lineNo = 1;
        // Reset column number.
        colNo = 0;
        // Allow replacements.
        allowReplacement = replacementCount != maxReplacements;
    }

    static class PaddingWrapper implements CharSequence {
        private static final char PADDING = (char) 0;

        private final CharSequence charSequence;
        private final boolean pad;

        PaddingWrapper(final CharSequence charSequence, final boolean pad) {
            this.charSequence = charSequence;
            this.pad = pad;
        }

        @Override
        public int length() {
            if (pad) {
                return charSequence.length() + 1;
            }

            return charSequence.length();
        }

        @Override
        public char charAt(final int index) {
            if (pad) {
                if (index <= 0) {
                    return PADDING;
                }
                return charSequence.charAt(index - 1);
            }

            return charSequence.charAt(index);
        }

        @Override
        public CharSequence subSequence(final int start, final int end) {
            if (pad) {
                if (start < 1) {
                    throw new IllegalArgumentException("Unexpected start for subsequence");
                }
                return new SubSequence(charSequence, start - 1, end);
            }
            return new SubSequence(charSequence, start, end);
        }

        @Override
        public String toString() {
            if (pad) {
                return PADDING + charSequence.toString();
            }

            return charSequence.toString();
        }
    }

    static class SubSequence implements CharSequence {
        private final CharSequence charSequence;
        private final int start;
        private final int end;

        SubSequence(final CharSequence charSequence, final int start, final int end) {
            this.charSequence = charSequence;
            this.start = start;
            this.end = end;
        }

        @Override
        public int length() {
            return end - start;
        }

        @Override
        public char charAt(final int index) {
            return charSequence.charAt(start + index);
        }

        @Override
        public CharSequence subSequence(final int start, final int end) {
            return new SubSequence(charSequence, this.start + start, this.start + end);
        }

        @Override
        public String toString() {
            return charSequence.subSequence(start, end).toString();
        }
    }

    private static class InBuffer extends CharBuffer {
        private final int initialSize;
        private final int capacity;
        private final int halfCapacity;

        private Reader reader;
        private boolean eof;

        InBuffer(final Reader reader, final int initialSize, final int capacity) {
            super(initialSize);
            this.reader = reader;

            if (initialSize < 8) {
                throw new IllegalStateException("The initial size must be greater than or equal to 8");
            }
            if (capacity < initialSize) {
                throw new IllegalStateException("Capacity must be greater than or equal to " + initialSize);
            }

            this.capacity = capacity;
            this.initialSize = initialSize;
            this.halfCapacity = capacity / 2;
        }

        void fillBuffer() throws IOException {
            // Only fill the buffer if we haven't reached the end of the file.
            if (!eof) {
                int i = 0;

                // Only fill the buffer if the length of remaining characters is
                // less than half the capacity.
                if (length <= halfCapacity) {
                    // If we processed more than half of the capacity then we need
                    // to shift the buffer up so that the offset becomes 0. This
                    // will give us
                    // room to fill more of the buffer.
                    if (offset >= halfCapacity) {
                        System.arraycopy(buffer, offset, buffer, 0, length);
                        offset = 0;
                    }

                    // Now fill any remaining capacity.
                    int maxLength = capacity - offset;
                    while (maxLength > length && (i = reader.read()) != -1) {
                        final char c = (char) i;

                        // If we have filled the buffer then double the size of
                        // it up to the maximum capacity.
                        if (buffer.length == offset + length) {
                            int newLen = buffer.length * 2;
                            if (newLen == 0) {
                                newLen = initialSize;
                            } else if (newLen > capacity) {
                                newLen = capacity;
                            }
                            final char[] tmp = new char[newLen];
                            System.arraycopy(buffer, offset, tmp, 0, length);
                            offset = 0;
                            buffer = tmp;

                            // Now the offset has been reset to 0 we should set
                            // the max length to be the maximum capacity.
                            maxLength = capacity;
                        }

                        buffer[offset + length] = c;
                        length++;
                    }
                }

                // Determine if we reached the end of the file.
                eof = i == -1;
            }
        }

        public boolean isEof() {
            return eof;
        }
    }


    private static class OutBuffer {
        private StringBuilder sb = new StringBuilder();
        private int offset;

        void append(final CharSequence s, final int start, final int end) {
            sb.append(s, start, end);
        }

        void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
            sb.getChars(srcBegin + offset, srcEnd + offset, dst, dstBegin);
        }

        int charAt(final int index) {
            return sb.charAt(offset + index);
        }

        int length() {
            return sb.length() - offset;
        }

        void move(final int len) {
            if (len >= length()) {
                clear();
            } else {
                offset += len;
                if (offset > 1000) {
                    sb.delete(0, offset);
                    offset = 0;
                }
            }
        }

        void clear() {
            sb.setLength(0);
            offset = 0;
        }

        @Override
        public String toString() {
            return sb.substring(offset);
        }
    }

    static class Builder {
        private Reader reader;
        private String find;
        private String replacement = "";
        private int maxReplacements = -1;
        private boolean regex;
        private boolean dotAll;
        private int bufferSize;
        private LocationFactory locationFactory;
        private ErrorReceiver errorReceiver;
        private String elementId;

        public Builder reader(final Reader reader) {
            this.reader = reader;
            return this;
        }

        public Builder find(final String find) {
            this.find = find;
            return this;
        }

        public Builder replacement(final String replacement) {
            if (replacement != null) {
                this.replacement = replacement;
            }
            return this;
        }

        public Builder maxReplacements(final int maxReplacements) {
            this.maxReplacements = maxReplacements;
            return this;
        }

        public Builder regex(final boolean regex) {
            this.regex = regex;
            return this;
        }

        public Builder dotAll(final boolean dotAll) {
            this.dotAll = dotAll;
            return this;
        }

        public Builder bufferSize(final int bufferSize) {
            this.bufferSize = bufferSize;
            return this;
        }

        public Builder locationFactory(final LocationFactory locationFactory) {
            this.locationFactory = locationFactory;
            return this;
        }

        public Builder errorReceiver(final ErrorReceiver errorReceiver) {
            this.errorReceiver = errorReceiver;
            return this;
        }

        public Builder elementId(final String elementId) {
            this.elementId = elementId;
            return this;
        }

        public FindReplaceFilter build() {
            return new FindReplaceFilter(reader, find, replacement, maxReplacements, regex, dotAll, bufferSize, locationFactory, errorReceiver, elementId);
        }
    }
}
