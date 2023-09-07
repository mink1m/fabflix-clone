ALTER TABLE sales
ADD COLUMN movieQuantity integer DEFAULT 1 AFTER saleDate;


ALTER TABLE movies
ADD FULLTEXT(title);