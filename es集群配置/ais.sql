/*
Navicat MySQL Data Transfer

Source Server         : glory
Source Server Version : 50553
Source Host           : localhost:3306
Source Database       : ais

Target Server Type    : MYSQL
Target Server Version : 50553
File Encoding         : 65001

Date: 2020-08-05 15:09:33
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for bo
-- ----------------------------
DROP TABLE IF EXISTS `bo`;
CREATE TABLE `bo` (
  `cid` int(11) NOT NULL,
  `hpcp` decimal(19,2) DEFAULT NULL,
  `time` datetime DEFAULT NULL,
  PRIMARY KEY (`cid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for dynamic
-- ----------------------------
DROP TABLE IF EXISTS `dynamic`;
CREATE TABLE `dynamic` (
  `did` int(11) NOT NULL AUTO_INCREMENT,
  `aistype` varchar(10) NOT NULL,
  `land_course` decimal(4,1) NOT NULL,
  `land_speed` decimal(4,1) NOT NULL,
  `lat` decimal(16,13) NOT NULL,
  `lng` decimal(16,13) NOT NULL,
  `mmsi` int(11) NOT NULL,
  `precion` varchar(10) NOT NULL,
  `ship_course` smallint(6) NOT NULL,
  `ship_length` smallint(6) DEFAULT NULL,
  `ship_name` varchar(40) DEFAULT NULL,
  `ship_state` varchar(40) NOT NULL,
  `ship_type` varchar(20) NOT NULL,
  `ship_width` smallint(6) DEFAULT NULL,
  `time` datetime NOT NULL,
  `turn_rate` smallint(6) DEFAULT NULL,
  PRIMARY KEY (`did`),
  KEY `mmsi` (`mmsi`),
  KEY `time` (`time`)
) ENGINE=InnoDB AUTO_INCREMENT=2578411 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for lnglat
-- ----------------------------
DROP TABLE IF EXISTS `lnglat`;
CREATE TABLE `lnglat` (
  `id` int(11) NOT NULL,
  `height` double DEFAULT NULL,
  `lat` double DEFAULT NULL,
  `lng` double DEFAULT NULL,
  `sn` double DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for poi
-- ----------------------------
DROP TABLE IF EXISTS `poi`;
CREATE TABLE `poi` (
  `id` int(11) NOT NULL,
  `a1` varchar(255) DEFAULT NULL,
  `a10` varchar(255) DEFAULT NULL,
  `a11` varchar(255) DEFAULT NULL,
  `a12` varchar(255) DEFAULT NULL,
  `a13` varchar(255) DEFAULT NULL,
  `a14` varchar(255) DEFAULT NULL,
  `a15` varchar(255) DEFAULT NULL,
  `a16` varchar(255) DEFAULT NULL,
  `a17` varchar(255) DEFAULT NULL,
  `a18` varchar(255) DEFAULT NULL,
  `a19` varchar(255) DEFAULT NULL,
  `a2` varchar(255) DEFAULT NULL,
  `a20` varchar(255) DEFAULT NULL,
  `a3` varchar(255) DEFAULT NULL,
  `a4` varchar(255) DEFAULT NULL,
  `a5` varchar(255) DEFAULT NULL,
  `a6` varchar(255) DEFAULT NULL,
  `a7` varchar(255) DEFAULT NULL,
  `a8` varchar(255) DEFAULT NULL,
  `a9` varchar(255) DEFAULT NULL,
  `day` varchar(255) DEFAULT NULL,
  `mon` varchar(255) DEFAULT NULL,
  `year` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for sheet1
-- ----------------------------
DROP TABLE IF EXISTS `sheet1`;
CREATE TABLE `sheet1` (
  `id` int(11) NOT NULL,
  `lat` varchar(255) DEFAULT NULL,
  `lng` varchar(255) DEFAULT NULL,
  `modal1` varchar(255) DEFAULT NULL,
  `modal2` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for static
-- ----------------------------
DROP TABLE IF EXISTS `static`;
CREATE TABLE `static` (
  `sid` int(11) NOT NULL AUTO_INCREMENT,
  `aistype` varchar(10) NOT NULL,
  `called_name` varchar(20) DEFAULT NULL,
  `destination` varchar(40) NOT NULL,
  `eta` varchar(15) NOT NULL,
  `imo` varchar(15) NOT NULL,
  `mmsi` int(11) NOT NULL,
  `po_type` varchar(20) NOT NULL,
  `ship_length` smallint(6) NOT NULL,
  `ship_name` varchar(40) DEFAULT NULL,
  `ship_type` varchar(20) NOT NULL,
  `ship_width` smallint(6) NOT NULL,
  `time` datetime NOT NULL,
  `water_depth` decimal(4,1) NOT NULL,
  PRIMARY KEY (`sid`),
  KEY `smmsi` (`mmsi`)
) ENGINE=InnoDB AUTO_INCREMENT=5341 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for test_jpa
-- ----------------------------
DROP TABLE IF EXISTS `test_jpa`;
CREATE TABLE `test_jpa` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `gender` varchar(4) DEFAULT NULL,
  `laname` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `uid` int(11) NOT NULL AUTO_INCREMENT,
  `password` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  `power` varchar(255) DEFAULT NULL,
  `last_lng` varchar(255) DEFAULT NULL,
  `lat_lat` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`uid`)
) ENGINE=MyISAM AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
