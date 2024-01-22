CREATE TABLE IF NOT EXISTS authors(
    id UUID PRIMARY KEY,
    first_name TEXT,
    last_name TEXT,
    birth_date DATE,
    state TEXT NOT NULL DEFAULT 'ACTIVE',
    version INTEGER NOT NULL DEFAULT 0,
    deleted BOOLEAN,
    created TIMESTAMP WITH TIME ZONE,
    created_by TEXT,
    modified TIMESTAMP WITH TIME ZONE,
    modified_by TEXT
);
CREATE TABLE IF NOT EXISTS author_tags(
    author_id UUID,
    key TEXT NOT NULL,
    value TEXT,
    PRIMARY KEY (author_id, key),
    FOREIGN KEY(author_id) REFERENCES authors(id)
);

CREATE TABLE IF NOT EXISTS books(
    id UUID PRIMARY KEY,
    title TEXT,
    published_date TIMESTAMP WITH TIME ZONE,
    author_id UUID,
    state TEXT NOT NULL DEFAULT 'ACTIVE',
    version INTEGER NOT NULL DEFAULT 0,
    deleted BOOLEAN,
    created TIMESTAMP WITH TIME ZONE,
    created_by TEXT,
    modified TIMESTAMP WITH TIME ZONE,
    modified_by TEXT,
    FOREIGN KEY(author_id) REFERENCES authors(id)
);
CREATE TABLE IF NOT EXISTS book_tags(
    book_id UUID,
    key TEXT NOT NULL,
    value TEXT,
    PRIMARY KEY (book_id, key),
    FOREIGN KEY(book_id) REFERENCES books(id)
);

CREATE TABLE IF NOT EXISTS categories(
    id UUID PRIMARY KEY,
    name TEXT,
    state TEXT NOT NULL DEFAULT 'ACTIVE',
    version INTEGER NOT NULL DEFAULT 0,
    deleted BOOLEAN,
    created TIMESTAMP WITH TIME ZONE,
    created_by TEXT,
    modified TIMESTAMP WITH TIME ZONE,
    modified_by TEXT
);
CREATE TABLE IF NOT EXISTS category_tags(
    category_id UUID,
    key TEXT NOT NULL,
    value TEXT,
    PRIMARY KEY (category_id, key),
    FOREIGN KEY(category_id) REFERENCES categories(id)
);

CREATE TABLE IF NOT EXISTS book_categories(
    book_id UUID NOT NULL,
    category_id UUID NOT NULL,
    FOREIGN KEY(book_id) REFERENCES books(id),
    FOREIGN KEY(category_id) REFERENCES categories(id),
    UNIQUE(book_id, category_id)
);
