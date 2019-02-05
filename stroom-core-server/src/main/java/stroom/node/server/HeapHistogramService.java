package stroom.node.server;

import com.google.common.base.Preconditions;
import com.sun.management.DiagnosticCommandMBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import stroom.util.logging.LambdaLogger;
import sun.management.ManagementFactoryHelper;

import javax.inject.Inject;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Class for generating a java heap map histogram using the gcClassHistogram action of the
 * {@link DiagnosticCommandMBean}
 */
@SuppressWarnings("unused")
@Component
class HeapHistogramService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HeapHistogramService.class);

    static final String CLASS_NAME_MATCH_REGEX_PROP_KEY = "stroom.node.status.heapHistogram.classNameMatchRegex";
    static final String ANON_ID_REGEX_PROP_KEY = "stroom.node.status.heapHistogram.classNameReplacementRegex";

    private static final String ACTION_NAME = "gcClassHistogram";
    private static String ID_REPLACEMENT = "--ID-REMOVED--";

    private static final int STRING_TRUNCATE_LIMIT = 200;

    private final StroomPropertyService stroomPropertyService;
    private final Pattern lineMatchPattern;

    @SuppressWarnings("unused")
    @Inject
    HeapHistogramService(final StroomPropertyService stroomPropertyService) {
        this.stroomPropertyService = stroomPropertyService;
        this.lineMatchPattern = Pattern.compile("\\s*\\d+:\\s+(?<instances>\\d+)\\s+(?<bytes>\\d+)\\s+(?<class>.*)");
    }

    /**
     * Generates a jmap heap histogram by calling the 'jmap' binary on the filesystem.  Will
     * block until jmap completes/fails. Reads the content of stdout and parses it to return a
     * list of {@link HeapHistogramEntry}
     */
    List<HeapHistogramEntry> generateHeapHistogram() {
        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];

        DiagnosticCommandMBean dcmd = ManagementFactoryHelper.getDiagnosticCommandMBean();
        String[] emptyStringArgs = {};
        Object[] dcmdArgs = { emptyStringArgs };
        String[] signature = { String[].class.getName() };
        Object output = null;
        LOGGER.info("Executing a heap histogram using action {}", ACTION_NAME);
        try {
            output = dcmd.invoke(ACTION_NAME, dcmdArgs, signature);
        } catch (MBeanException | ReflectionException e) {
            LOGGER.error("Error invoking action {}", ACTION_NAME, e);
        }

        final List<HeapHistogramEntry> heapHistogramEntries;
        if (output == null) {
            LOGGER.warn("Heap histogram produced no output", ACTION_NAME);
            heapHistogramEntries = Collections.emptyList();
        } else if (output instanceof String){
            heapHistogramEntries = processOutput((String) output);
        } else {
            throw new RuntimeException(LambdaLogger.buildMessage("Unexpected type {}", output.getClass().getName()));
        }

        return heapHistogramEntries;
    }

    private static String getTruncatedStr(final String str) {
        if (str != null && str.length() > STRING_TRUNCATE_LIMIT) {
            return str.substring(0, STRING_TRUNCATE_LIMIT) + "...TRUNCATED...";
        } else {
            return str;
        }
    }

    private static Predicate<String> getClassNameMatchPredicate(final StroomPropertyService stroomPropertyService) {
        String classNameRegexStr = stroomPropertyService.getProperty(CLASS_NAME_MATCH_REGEX_PROP_KEY);

        if (classNameRegexStr == null || classNameRegexStr.isEmpty()) {
            //no prop value so return an always true predicate
            return str -> true;
        } else {
            try {
                return Pattern.compile(classNameRegexStr).asPredicate();
            } catch (Exception e) {
                throw new RuntimeException(
                        LambdaLogger.buildMessage("Error compiling regex string [{}]", classNameRegexStr), e);
            }
        }
    }

    private Function<String, String> getClassReplacementMapper() {
        final String anonymousIdRegex = stroomPropertyService.getProperty(ANON_ID_REGEX_PROP_KEY);

        if (anonymousIdRegex == null || anonymousIdRegex.isEmpty()) {
            return Function.identity();
        } else {
            try {
                final Pattern pattern = Pattern.compile(anonymousIdRegex);
                return className -> pattern.matcher(className).replaceAll(ID_REPLACEMENT);
            } catch (Exception e) {
                LOGGER.error("Value [{}] for property [{}] is not valid regex",
                        anonymousIdRegex, ANON_ID_REGEX_PROP_KEY, e);
                return Function.identity();
            }
        }
    }

    private Function<String, Optional<HeapHistogramEntry>> buildLineToEntryMapper(final Function<String, String> classNameReplacer) {
        Preconditions.checkNotNull(classNameReplacer);
        return line -> {
            Matcher matcher = lineMatchPattern.matcher(line);
            if (matcher.matches()) {
                //if this is a data row then extract the values of interest
                final long instances = Long.parseLong(matcher.group("instances"));
                final long bytes = Long.parseLong(matcher.group("bytes"));
                final String className = matcher.group("class");
                final String newClassName = classNameReplacer.apply(className);
                LOGGER.trace("className [{}], newClassName [{}]", className, newClassName);

                return Optional.of(new HeapHistogramEntry(newClassName, instances, bytes));
            } else {
                LOGGER.trace("Ignoring jamp histogram line [{}]", line);
                return Optional.empty();
            }
        };
    }

    private List<HeapHistogramEntry> processOutput(final String output) {
        Preconditions.checkNotNull(output);

        try {
            Predicate<String> classNamePredicate = getClassNameMatchPredicate(stroomPropertyService);
            Function<String, String> classNameReplacer = getClassReplacementMapper();
            Function<String, Optional<HeapHistogramEntry>> lineToEntryMapper = buildLineToEntryMapper(classNameReplacer);

            String[] lines = output.split("\\r?\\n");

            LOGGER.debug("processing {} lines of stdout", lines.length);

            final List<HeapHistogramService.HeapHistogramEntry> histogramEntries = Arrays.stream(lines)
                    .map(lineToEntryMapper)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .filter(heapHistogramEntry ->
                            classNamePredicate.test(heapHistogramEntry.getClassName()))
                    .collect(Collectors.toList());

            LOGGER.debug("histogramEntries size [{}]", histogramEntries.size());
            if (histogramEntries.size() == 0) {
                LOGGER.error("Something has gone wrong filtering the heap histogram, zero entries returned");
            }
            return histogramEntries;

        } catch (Exception e) {
            throw new RuntimeException(LambdaLogger.buildMessage("Error processing output string [{}]",
                    getTruncatedStr(output)), e);
        }
    }

    static class HeapHistogramEntry {
        private final String className;
        private final long instances;
        private final long bytes;

        HeapHistogramEntry(final String className, final long instances, final long bytes) {
            this.className = Preconditions.checkNotNull(className);
            this.instances = instances;
            this.bytes = bytes;
        }

        String getClassName() {
            return className;
        }

        long getInstances() {
            return instances;
        }

        long getBytes() {
            return bytes;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final HeapHistogramEntry that = (HeapHistogramEntry) o;

            if (instances != that.instances) return false;
            if (bytes != that.bytes) return false;
            return className.equals(that.className);
        }

        @Override
        public int hashCode() {
            int result = className.hashCode();
            result = 31 * result + (int) (instances ^ (instances >>> 32));
            result = 31 * result + (int) (bytes ^ (bytes >>> 32));
            return result;
        }

        @Override
        public String toString() {
            return "HeapHistogramEntry{" +
                    "className='" + className + '\'' +
                    ", instances=" + instances +
                    ", bytes=" + bytes +
                    '}';
        }
    }
}
