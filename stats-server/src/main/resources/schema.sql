DROP TABLE IF EXISTS views CASCADE;

CREATE TABLE views (
    view_id INT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    app VARCHAR NOT NULL,
    uri VARCHAR NOT NULL,
    ip VARCHAR NOT NULL,
    view_date TIMESTAMP NOT NULL,
    CONSTRAINT pk_views PRIMARY KEY (view_id)
);