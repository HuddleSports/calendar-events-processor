CREATE TABLE `JdoValue` (
  `key` varchar(255) NOT NULL PRIMARY KEY,
  `binaryData` varbinary(1000) DEFAULT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
);

CREATE TABLE `Location` (
  `id` int(11) NOT NULL AUTO_INCREMENT UNIQUE,
  `name` varchar(255) DEFAULT NULL,
  `state` varchar(255) NOT NULL,
  `city` varchar(255) NOT NULL,
  `address` varchar(255) NOT NULL,
  `calendarId` varchar(255) NOT NULL PRIMARY KEY
);

CREATE TABLE `Event` (
  `id` int(11) NOT NULL AUTO_INCREMENT unique,
  `calendarEventId` varchar(255) NOT NULL PRIMARY KEY,
  `type` varchar(255) DEFAULT NULL,
  `category` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `price` int(11) DEFAULT NULL,
  `organizer` varchar(255) DEFAULT NULL,
  `description` varchar(255) NOT NULL,
  `startTime` datetime NOT NULL,
  `endTime` datetime NOT NULL,
  `locationId` int(11) NOT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT `locationId` FOREIGN KEY (`locationId`) REFERENCES `Location` (`id`)
);

CREATE TABLE `PendingJob` (
  `id` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `startDateInc` date NOT NULL,
  `endDateExl` date DEFAULT NULL,
  `nextDate` date NOT NULL,
  `status` enum('PENDING','COMPLETED','CANCELED') DEFAULT 'PENDING',
  `registerCallback` boolean NOT NULL DEFAULT FALSE,
  `locationId` int(11) NOT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT `pendingJobLocationId` FOREIGN KEY (`locationId`) REFERENCES `Location` (`id`)
);