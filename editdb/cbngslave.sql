DROP DATABASE IF EXISTS `cbng_editdb`;
CREATE DATABASE `cbng_editdb`;
USE `cbng_editdb`;

DROP SERVER IF EXISTS 'cbng_editdb_master_server';

CREATE SERVER 'cbng_editdb_master_server'
FOREIGN DATA WRAPPER mysql
OPTIONS (
	HOST 'gamma.cluenet.org',
	DATABASE 'cbng_editdb_master',
	USER 'cbng_editdb_slv',
	PASSWORD 'cbng-editdb-slave'
);

CREATE TABLE `editset_remote` (
	`edittype`                     VARCHAR(32)    NOT NULL DEFAULT 'change',
	`editid`                       INTEGER        NOT NULL,
	`comment`                      VARBINARY(255) NOT NULL,
	`user`                         VARBINARY(255) NOT NULL,
	`user_edit_count`              INTEGER        NOT NULL,
	`user_distinct_pages`          INTEGER        NOT NULL,
	`user_warns`                   INTEGER        NOT NULL,
	`prev_user`                    VARBINARY(255) NOT NULL,
	`user_reg_time`                DATETIME       NOT NULL,
	`common_page_made_time`        DATETIME       NOT NULL,
	`common_title`                 VARBINARY(255) NOT NULL,
	`common_namespace`             VARCHAR(64)    NOT NULL,
	`common_creator`               VARBINARY(255) NOT NULL,
	`common_num_recent_edits`      INTEGER        NOT NULL,
	`common_num_recent_reversions` INTEGER        NOT NULL,
	`current_minor`                TINYINT(1)     NOT NULL DEFAULT 0,
	`current_timestamp`            DATETIME       NOT NULL,
	`current_text`                 MEDIUMBLOB     NOT NULL,
	`previous_timestamp`           DATETIME       NOT NULL,
	`previous_text`                MEDIUMBLOB     NOT NULL,
	`isvandalism`                  TINYINT(1)     NOT NULL,
	`isactive`                     TINYINT(1)     NOT NULL DEFAULT 0,
	`updated`                      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	`source`                       VARCHAR(128)       NULL,
	`reviewers`                    INTEGER            NULL,
	`reviewers_agreeing`           INTEGER            NULL,
	
	PRIMARY KEY (`editid`),
	KEY (`updated`)
)
ENGINE=FEDERATED
CONNECTION='cbng_editdb_master_server/editset';

CREATE TABLE `editset` (
	`edittype`                     VARCHAR(32)    NOT NULL DEFAULT 'change',
	`editid`                       INTEGER        NOT NULL,
	`comment`                      VARBINARY(255) NOT NULL,
	`user`                         VARBINARY(255) NOT NULL,
	`user_edit_count`              INTEGER        NOT NULL,
	`user_distinct_pages`          INTEGER        NOT NULL,
	`user_warns`                   INTEGER        NOT NULL,
	`prev_user`                    VARBINARY(255) NOT NULL,
	`user_reg_time`                DATETIME       NOT NULL,
	`common_page_made_time`        DATETIME       NOT NULL,
	`common_title`                 VARBINARY(255) NOT NULL,
	`common_namespace`             VARCHAR(64)    NOT NULL,
	`common_creator`               VARBINARY(255) NOT NULL,
	`common_num_recent_edits`      INTEGER        NOT NULL,
	`common_num_recent_reversions` INTEGER        NOT NULL,
	`current_minor`                TINYINT(1)     NOT NULL DEFAULT 0,
	`current_timestamp`            DATETIME       NOT NULL,
	`current_text`                 MEDIUMBLOB     NOT NULL,
	`previous_timestamp`           DATETIME       NOT NULL,
	`previous_text`                MEDIUMBLOB     NOT NULL,
	`isvandalism`                  TINYINT(1)     NOT NULL,
	`isactive`                     TINYINT(1)     NOT NULL DEFAULT 0,
	`updated`                      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	`source`                       VARCHAR(128)       NULL,
	`reviewers`                    INTEGER            NULL,
	`reviewers_agreeing`           INTEGER            NULL,
	
	PRIMARY KEY (`editid`),
	INDEX USING BTREE (`updated`),
	INDEX USING BTREE (`isactive`),
	INDEX USING BTREE (`source`)
)
ENGINE=InnoDB
ROW_FORMAT=COMPRESSED 
KEY_BLOCK_SIZE=4;

CREATE TABLE `lastupdated_remote` (
	`lastupdated` TIMESTAMP NOT NULL
)
ENGINE=FEDERATED
CONNECTION='cbng_editdb_master_server/lastupdated';

CREATE TABLE `lastupdated` (
	`lastupdated` TIMESTAMP NOT NULL
);

-- INSERT INTO `lastupdated` SELECT * FROM `lastupdated_remote`;
-- INSERT INTO `editset` SELECT * FROM `editset_remote`;

DELIMITER |
CREATE PROCEDURE update_data()
BEGIN
	SET @lastupdated = (SELECT `lastupdated` FROM `lastupdated` LIMIT 1);
	CREATE TEMPORARY TABLE `editset_temp` SELECT * FROM `editset_remote` WHERE `editset_remote`.`updated` > @lastupdated;
	REPLACE INTO `editset` SELECT * FROM `editset_temp`;
	DROP TABLE `editset_temp`;
	UPDATE `lastupdated` SET `lastupdated` = (SELECT `lastupdated` FROM `lastupdated_remote` LIMIT 1);
END;
|