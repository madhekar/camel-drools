CREATE SEQUENCE IF NOT EXISTS sessioninfo_seq;
CREATE TABLE IF NOT EXISTS sessioninfo
  (
    "ID"    NUMBER(32,0) NOT NULL,
    "DIRTY" NUMBER(1,0) NOT NULL,
    "LASTMODIFICATIONDATE" TIMESTAMP (6) ,
    "RULESBYTEARRAY" BLOB,
    "STARTDATE" TIMESTAMP (6),
    "TASK_ID"  VARCHAR2(100),
    "TASK_VERSION" VARCHAR2(100),
    "NEXT_TIMER" TIMESTAMP (6),
    "DISCRIMINATOR" VARCHAR2(100),
    CONSTRAINT "SESSIONINFO_PK" PRIMARY KEY ("ID")
  );
  
CREATE TABLE IF NOT EXISTS objectinfo
  (
    "ID"         NUMBER NOT NULL,
    "SESSION_ID" NUMBER NOT NULL,
    "OBJECT_ID"  NUMBER,
    "TYPE"       VARCHAR2(1000),
    "TIMESTAMP" TIMESTAMP (6),
    CONSTRAINT "OBJECTINFO_PK" PRIMARY KEY ("ID")
  );
  
INSERT INTO sessioninfo VALUES (1234, 0, current_timestamp-5, null, current_timestamp-1, '123', '1', current_timestamp, 'task');
INSERT INTO objectinfo VALUES (12, 1234, 1, 'Task', current_timestamp-10);
INSERT INTO objectinfo VALUES (13, 1234, 2, 'Task', current_timestamp-10);

INSERT INTO sessioninfo VALUES (1235, 1, current_timestamp-5, null, current_timestamp-1, '123', '2', current_timestamp, 'task');
INSERT INTO objectinfo VALUES (14, 1235, 3, 'Task', current_timestamp-10);
