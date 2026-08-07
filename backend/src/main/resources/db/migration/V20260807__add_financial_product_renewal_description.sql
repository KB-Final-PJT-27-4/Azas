-- Existing shared RDS instances created before PRODUCT-4 need this once.
-- Fresh installations already receive the column from schema.sql.
ALTER TABLE financial_product
    ADD COLUMN renewal_description VARCHAR(500) NULL
    AFTER contract_period_months;

UPDATE financial_product
SET renewal_description = '재예치 가능 여부는 상품 약관을 확인하세요.'
WHERE renewal_description IS NULL;
