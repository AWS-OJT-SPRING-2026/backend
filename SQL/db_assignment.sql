CREATE TABLE assignments (
    assignmentid SERIAL PRIMARY KEY,
    classid INTEGER,
    userid INTEGER,
    teacherid INTEGER,
    title VARCHAR(255),
    assignment_type VARCHAR(50),
    format VARCHAR(50),
    start_time TIMESTAMP(6),
    end_time TIMESTAMP(6),
    deadline TIMESTAMP(6),
    duration_minutes INTEGER,
    status VARCHAR(50),
    created_at TIMESTAMP(6),
    updated_at TIMESTAMP(6),

    FOREIGN KEY (userid)
        REFERENCES users(userid)
        ON DELETE CASCADE,

    FOREIGN KEY (teacherid)
        REFERENCES teachers(teacherid)
        ON DELETE CASCADE
);

CREATE TABLE assignment_questions (
    assignmentid INTEGER,
    questionid INTEGER,

    PRIMARY KEY (assignmentid, questionid),

    FOREIGN KEY (assignmentid)
        REFERENCES assignments(assignmentid)
        ON DELETE CASCADE,

    FOREIGN KEY (questionid)
        REFERENCES questions(id)
        ON DELETE CASCADE
);

CREATE TABLE submissions (
    submissionid SERIAL PRIMARY KEY,
    assignmentid INTEGER,
    userid INTEGER,
    score NUMERIC(5,2),
    time_taken INTEGER,
    started_at TIMESTAMP(6),
    submitted_at TIMESTAMP(6),
    expired_at TIMESTAMP(6),

    FOREIGN KEY (assignmentid)
        REFERENCES assignments(assignmentid)
        ON DELETE CASCADE,

    FOREIGN KEY (userid)
        REFERENCES users(userid)
        ON DELETE CASCADE
);

CREATE TABLE submission_answers (
    answerid SERIAL PRIMARY KEY,
    submissionid INTEGER,
    questionid INTEGER,
    answer_ref_id INTEGER,
    selected_answer VARCHAR(255),
    is_correct BOOLEAN,

    FOREIGN KEY (submissionid)
        REFERENCES submissions(submissionid)
        ON DELETE CASCADE,

    FOREIGN KEY (questionid)
        REFERENCES questions(id)
        ON DELETE CASCADE,

    FOREIGN KEY (answer_ref_id)
        REFERENCES answers(id)
        ON DELETE CASCADE
);