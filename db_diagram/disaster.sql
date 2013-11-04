SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

CREATE SCHEMA IF NOT EXISTS `disaster` DEFAULT CHARACTER SET utf8 ;
USE `disaster` ;

-- -----------------------------------------------------
-- Table `disaster`.`disasters`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `disaster`.`disasters` ;

CREATE  TABLE IF NOT EXISTS `disaster`.`disasters` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `year` INT NULL ,
  `name` VARCHAR(128) NOT NULL ,
  `type` INT NULL ,
  `start_time` DATETIME NULL ,
  `end_time` DATETIME NULL ,
  `damage` FLOAT NULL ,
  `casualty` FLOAT NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `disaster`.`disaster_news`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `disaster`.`disaster_news` ;

CREATE  TABLE IF NOT EXISTS `disaster`.`disaster_news` (
  `news_id` BIGINT NOT NULL AUTO_INCREMENT ,
  `disaster_id` INT NOT NULL ,
  `title` VARCHAR(45) NULL ,
  `authors` VARCHAR(45) NULL ,
  `publisher` VARCHAR(45) NULL ,
  `post_date` DATE NULL ,
  `url` VARCHAR(500) NULL ,
  `html` LONGTEXT NULL ,
  `fetchtime` TIMESTAMP ,
  PRIMARY KEY (`news_id`) ,
  CONSTRAINT `news.FK`
    FOREIGN KEY (`disaster_id` )
    REFERENCES `disaster`.`disasters` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
PACK_KEYS = DEFAULT;


-- -----------------------------------------------------
-- Table `disaster`.`events`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `disaster`.`events` ;

CREATE  TABLE IF NOT EXISTS `disaster`.`events` (
  `event_id` BIGINT NOT NULL AUTO_INCREMENT ,
  `disaster_id` INT NOT NULL ,
  `url` VARCHAR(500) NULL ,
  `content` VARCHAR(1024) NULL ,
  `event_date` DATETIME NULL ,
  `location` VARCHAR(128) NULL ,
  `latitude` DECIMAL(13,10) NULL ,
  `longtitude` DECIMAL(13,10) NULL ,
  PRIMARY KEY (`event_id`) ,
  INDEX `id_idx` (`disaster_id` ASC) ,
  CONSTRAINT `events.FK`
    FOREIGN KEY (`disaster_id` )
    REFERENCES `disaster`.`disasters` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

USE `disaster` ;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
