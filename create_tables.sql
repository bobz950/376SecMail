USE secmail;

set foreign_key_checks=0;

drop table user;
drop table message;
drop table tag;
drop table message_tag;
drop table message_recipient;

CREATE TABLE user(
		user_id INT AUTO_INCREMENT,
		user_address VARCHAR(40) NOT NULL,
    user_password VARCHAR(40) NOT NULL,
    user_salt VARCHAR(40),
    primary key(user_id)
);

CREATE TABLE message(
	message_id INT AUTO_INCREMENT,
  sender_id INT NOT NULL,
  message_subject VARCHAR(100),
  message_content VARCHAR(1000),
  message_attatchment BLOB,
  message_date DATETIME,
	PRIMARY KEY (message_id),
  FOREIGN KEY (sender_id) REFERENCES user(user_id)
	);

CREATE TABLE tag(
	tag_id INT auto_increment,
    tag_name VARCHAR(40),

    primary key ( tag_id)
);

CREATE TABLE message_tag(
	tag_id INT,
    user_id INT,

    FOREIGN KEY (tag_id) REFERENCES tag(tag_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id)
);

CREATE TABLE message_recipient (
    user_id INT,
    message_id INT,
    FOREIGN KEY (user_id)
        REFERENCES user (user_id),
    FOREIGN KEY (message_id)
        REFERENCES message (message_id)
);

INSERT INTO user(user_id, user_address, user_password)
VALUES
(  0, "test", "test");

INSERT INTO user(user_id, user_address, user_password)
VALUES
(  0, "test2", "test2");

Select * from user;
