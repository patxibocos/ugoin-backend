CREATE TABLE user (
  id       BIGINT             NOT NULL AUTO_INCREMENT,
  name     VARCHAR(30)        NOT NULL,
  email    VARCHAR(255)                DEFAULT NULL,
  password VARCHAR(40)        NOT NULL,
  salt     VARCHAR(40)        NOT NULL,
  token    VARCHAR(36) UNIQUE NOT NULL
);

CREATE TABLE source (
  id   BIGINT      NOT NULL AUTO_INCREMENT,
  name VARCHAR(30) NOT NULL
);

CREATE TABLE follow (
  id        BIGINT      NOT NULL AUTO_INCREMENT,
  follower  BIGINT      NOT NULL,
  following BIGINT      NOT NULL,
  status    VARCHAR(30) NOT NULL,
  created   DATETIME    NOT NULL,
  source    BIGINT      NOT NULL,
  FOREIGN KEY (following) REFERENCES user (id),
  FOREIGN KEY (source) REFERENCES source (id)
);

CREATE TABLE device (
  id   BIGINT              NOT NULL AUTO_INCREMENT,
  name VARCHAR(30)         NOT NULL,
  push VARCHAR(255) UNIQUE NOT NULL,
  user BIGINT              NOT NULL,
  FOREIGN KEY (user) REFERENCES user (id)
);

CREATE TABLE telegram_user (
  id         BIGINT      NOT NULL AUTO_INCREMENT,
  first_name VARCHAR(30)          DEFAULT NULL,
  last_name  VARCHAR(30)          DEFAULT NULL,
  username   VARCHAR(32) NOT NULL,
  chat_id    BIGINT      NOT NULL
);

CREATE TABLE twitter_user (
  id       BIGINT      NOT NULL AUTO_INCREMENT,
  username VARCHAR(15) NOT NULL
);

CREATE TABLE track (
  id       BIGINT   NOT NULL AUTO_INCREMENT,
  started  DATETIME NOT NULL,
  finished DATETIME,
  user     BIGINT   NOT NULL,
  FOREIGN KEY (user) REFERENCES user (id)
);

CREATE TABLE checkpoint (
  id        BIGINT   NOT NULL AUTO_INCREMENT,
  latitude  DOUBLE   NOT NULL,
  longitude DOUBLE   NOT NULL,
  tracked   DATETIME NOT NULL,
  track     BIGINT   NOT NULL,
  FOREIGN KEY (track) REFERENCES track (id)
);

INSERT INTO source (id, name) VALUES (1, 'Telegram');
INSERT INTO source (id, name) VALUES (2, 'Twitter');

CREATE TABLE source_message (
  id                BIGINT        NOT NULL AUTO_INCREMENT,
  processed         BIT(1)        NOT NULL,
  source_message_id BIGINT        NOT NULL,
  raw               VARCHAR(5120) NOT NULL,
  source            BIGINT        NOT NULL,
  FOREIGN KEY (source) REFERENCES source (id)
);