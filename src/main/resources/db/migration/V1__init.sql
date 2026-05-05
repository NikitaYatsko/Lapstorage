CREATE TABLE product (
                         id BIGSERIAL PRIMARY KEY,
                         price DECIMAL(10,2) NOT NULL,
                         description TEXT,
                         image_url TEXT NOT NULL
);
