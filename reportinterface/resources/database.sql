CREATE TABLE `reports` (
	`revertid` INT PRIMARY KEY NOT NULL,
	`timestamp` TIMESTAMP NOT NULL,
	`reporterid` INT NOT NULL,
	`reporter` VARCHAR(128) NOT NULL,
	`status` INT NOT NULL,
	
	INDEX USING BTREE (`reporterid`),
	INDEX USING BTREE (`status`)
) ENGINE=InnoDB;

CREATE TABLE `comments` (
	`commentid` INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
	`revertid` INT NOT NULL,
	`timestamp` TIMESTAMP NOT NULL,
	`userid` INT NOT NULL,
	`user` VARCHAR(128) NOT NULL,
	`comment` TEXT NOT NULL,
	
	INDEX USING BTREE (`revertid`),
	INDEX USING BTREE (`userid`)
) ENGINE=InnoDB;

CREATE TABLE `users` (
	`userid` INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
	`username` VARCHAR(128) NOT NULL,
	`password` VARCHAR(128) NOT NULL,
	`email` VARCHAR(128) NOT NULL,
	`admin` TINYINT(1) NOT NULL,
	`superadmin` TINYINT(1) NOT NULL,
	
	UNIQUE KEY (`username`)
) ENGINE=InnoDB;