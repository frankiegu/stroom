-- Stop NOTE level warnings about objects (not)? existing
SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0;

--
-- Rename the old QUERY table
--
DROP PROCEDURE IF EXISTS rename_query;
DELIMITER //
CREATE PROCEDURE rename_query ()
BEGIN
  IF EXISTS (
      SELECT TABLE_NAME
      FROM INFORMATION_SCHEMA.TABLES
      WHERE TABLE_NAME = 'QUERY') THEN

    SET @rename_sql='RENAME TABLE QUERY TO OLD_QUERY';
    PREPARE stmt FROM @rename_sql;
    EXECUTE stmt;
  END IF;
END//
DELIMITER ;
CALL rename_query();
DROP PROCEDURE rename_query;

--
-- Create the query table
--
CREATE TABLE IF NOT EXISTS query (
  id                    int(11) NOT NULL AUTO_INCREMENT,
  version               int(11) NOT NULL,
  create_time_ms        bigint(20) NOT NULL,
  create_user           varchar(255) NOT NULL,
  update_time_ms        bigint(20) NOT NULL,
  update_user           varchar(255) NOT NULL,
  dashboard_uuid        varchar(255) NOT NULL,
  component_id          varchar(255) NOT NULL,
  name                  varchar(255) NOT NULL,
  data                  longtext,
  favourite             bit(1) NOT NULL,
  PRIMARY KEY           (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Copy data into the query table
--
DROP PROCEDURE IF EXISTS copy_query;
DELIMITER //
CREATE PROCEDURE copy_query ()
BEGIN
  IF EXISTS (
      SELECT TABLE_NAME
      FROM INFORMATION_SCHEMA.TABLES
      WHERE TABLE_NAME = 'OLD_QUERY') THEN

    SET @insert_sql=''
        ' INSERT INTO query (id, version, create_time_ms, create_user, update_time_ms, update_user, dashboard_uuid, component_id, name, data, favourite)'
        ' SELECT ID, 1, CRT_MS, CRT_USER, UPD_MS, UPD_USER, DASH_UUID, QUERY_ID, NAME, DAT, FAVOURITE'
        ' FROM OLD_QUERY'
        ' WHERE ID > (SELECT COALESCE(MAX(id), 0) FROM query)'
        ' ORDER BY ID;';
    PREPARE insert_stmt FROM @insert_sql;
    EXECUTE insert_stmt;

    -- Work out what to set our auto_increment start value to
    SELECT CONCAT('ALTER TABLE query AUTO_INCREMENT = ', COALESCE(MAX(id) + 1, 1))
    INTO @alter_table_sql
    FROM query;

    PREPARE alter_table_stmt FROM @alter_table_sql;
    EXECUTE alter_table_stmt;
  END IF;
END//
DELIMITER ;
CALL copy_query();
DROP PROCEDURE copy_query;

SET SQL_NOTES=@OLD_SQL_NOTES;
