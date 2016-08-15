CREATE TABLE IF NOT EXISTS user (
    email CHAR(100) not null,
    name  CHAR(200) not null,
    primary key(email)
);

CREATE TABLE IF NOT EXISTS email (
    emailId INTEGER PRIMARY KEY AUTOINCREMENT,
    fromUser CHAR(100) not null,
    toUser CHAR(100) not null,
    message TEXT not null,
    received TIMESTAMP DEFAULT CURRENT_TIMESTAMP,    
    foreign key(toUser) REFERENCES user(email)
);