-- First drop constraints
ALTER TABLE users DROP CONSTRAINT users_pkey;

-- Change the column type
ALTER TABLE users
    ALTER COLUMN id TYPE BIGINT,
    ALTER COLUMN id SET DEFAULT nextval('users_id_seq'::regclass);

-- Re-add the primary key constraint
ALTER TABLE users ADD PRIMARY KEY (id);