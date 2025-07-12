DROP TABLE IF EXISTS BATCH_STEP_EXECUTION_CONTEXT;
DROP TABLE IF EXISTS BATCH_JOB_EXECUTION_CONTEXT;
DROP TABLE IF EXISTS BATCH_STEP_EXECUTION;
DROP TABLE IF EXISTS BATCH_JOB_EXECUTION_PARAMS;
DROP TABLE IF EXISTS BATCH_JOB_EXECUTION;
DROP TABLE IF EXISTS BATCH_JOB_INSTANCE;

CREATE TABLE BATCH_JOB_INSTANCE (
                                    JOB_INSTANCE_ID BIGINT NOT NULL PRIMARY KEY,
                                    VERSION BIGINT,
                                    JOB_NAME VARCHAR(100) NOT NULL,
                                    JOB_KEY VARCHAR(32) NOT NULL,
                                    constraint JOB_INST_UN unique (JOB_NAME, JOB_KEY)
) ENGINE=InnoDB;

CREATE TABLE BATCH_JOB_EXECUTION (
                                     JOB_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
                                     VERSION BIGINT,
                                     JOB_INSTANCE_ID BIGINT NOT NULL,
                                     CREATE_TIME DATETIME(6) NOT NULL,
                                     START_TIME DATETIME(6) DEFAULT NULL,
                                     END_TIME DATETIME(6) DEFAULT NULL,
                                     STATUS VARCHAR(10),
                                     EXIT_CODE VARCHAR(20),
                                     EXIT_MESSAGE VARCHAR(2500),
                                     LAST_UPDATED DATETIME(6),
                                     JOB_CONFIGURATION_LOCATION VARCHAR(2500) NULL,
                                     constraint JOB_INST_EXEC_FK foreign key (JOB_INSTANCE_ID)
                                         references BATCH_JOB_INSTANCE(JOB_INSTANCE_ID)
) ENGINE=InnoDB;

CREATE TABLE BATCH_JOB_EXECUTION_PARAMS (
                                            JOB_EXECUTION_ID BIGINT NOT NULL,
                                            PARAMETER_NAME VARCHAR(100) NOT NULL,
                                            PARAMETER_TYPE VARCHAR(100) NOT NULL,
                                            PARAMETER_VALUE TEXT,
                                            IDENTIFYING CHAR(1) NOT NULL,
                                            constraint JOB_EXEC_PARAMS_FK foreign key (JOB_EXECUTION_ID)
                                                references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ENGINE=InnoDB;

CREATE TABLE BATCH_STEP_EXECUTION (
                                      STEP_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
                                      VERSION BIGINT NOT NULL,
                                      STEP_NAME VARCHAR(100) NOT NULL,
                                      JOB_EXECUTION_ID BIGINT NOT NULL,
                                      START_TIME DATETIME(6) NOT NULL,
                                      END_TIME DATETIME(6) DEFAULT NULL,
                                      STATUS VARCHAR(10),
                                      COMMIT_COUNT BIGINT,
                                      READ_COUNT BIGINT,
                                      FILTER_COUNT BIGINT,
                                      WRITE_COUNT BIGINT,
                                      READ_SKIP_COUNT BIGINT,
                                      WRITE_SKIP_COUNT BIGINT,
                                      PROCESS_SKIP_COUNT BIGINT,
                                      ROLLBACK_COUNT BIGINT,
                                      EXIT_CODE VARCHAR(20),
                                      EXIT_MESSAGE VARCHAR(2500),
                                      LAST_UPDATED DATETIME(6),
                                      constraint JOB_EXEC_STEP_FK foreign key (JOB_EXECUTION_ID)
                                          references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ENGINE=InnoDB;

CREATE TABLE BATCH_STEP_EXECUTION_CONTEXT (
                                              STEP_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
                                              SHORT_CONTEXT VARCHAR(2500) NOT NULL,
                                              SERIALIZED_CONTEXT TEXT,
                                              constraint STEP_EXEC_CTX_FK foreign key (STEP_EXECUTION_ID)
                                                  references BATCH_STEP_EXECUTION(STEP_EXECUTION_ID)
) ENGINE=InnoDB;

CREATE TABLE BATCH_JOB_EXECUTION_CONTEXT (
                                             JOB_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
                                             SHORT_CONTEXT VARCHAR(2500) NOT NULL,
                                             SERIALIZED_CONTEXT TEXT,
                                             constraint JOB_EXEC_CTX_FK foreign key (JOB_EXECUTION_ID)
                                                 references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ENGINE=InnoDB;

-- 시퀀스 테이블 (ID 생성용)
CREATE TABLE BATCH_JOB_SEQ (
                               ID BIGINT NOT NULL,
                               UNIQUE_KEY CHAR(1) NOT NULL,
                               PRIMARY KEY (UNIQUE_KEY)
);

INSERT INTO BATCH_JOB_SEQ (ID, UNIQUE_KEY) VALUES (0, '0');

CREATE TABLE BATCH_JOB_EXECUTION_SEQ (
                                         ID BIGINT NOT NULL,
                                         UNIQUE_KEY CHAR(1) NOT NULL,
                                         PRIMARY KEY (ID)
);

ALTER TABLE BATCH_STEP_EXECUTION ADD COLUMN CREATE_TIME DATETIME;

INSERT INTO BATCH_JOB_EXECUTION_SEQ (ID, UNIQUE_KEY) VALUES (0, '0');

CREATE TABLE BATCH_STEP_EXECUTION_SEQ (
                                          ID BIGINT NOT NULL
);

INSERT INTO BATCH_STEP_EXECUTION_SEQ VALUES (0);

ALTER TABLE BATCH_STEP_EXECUTION MODIFY COLUMN START_TIME DATETIME DEFAULT NULL;

CREATE INDEX JOB_INST_EXEC_IDX ON BATCH_JOB_EXECUTION(JOB_INSTANCE_ID);
CREATE INDEX JOB_EXEC_EXIT_CODE_IDX ON BATCH_JOB_EXECUTION(EXIT_CODE);
CREATE INDEX STEP_EXEC_JOB_EXEC_IDX ON BATCH_STEP_EXECUTION(JOB_EXECUTION_ID);
CREATE INDEX STEP_EXEC_EXIT_CODE_IDX ON BATCH_STEP_EXECUTION(EXIT_CODE);