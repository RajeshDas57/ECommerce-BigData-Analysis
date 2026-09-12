-- ============================================
-- E-Commerce Sales Data Analysis Using Pig
-- ============================================

-- Load CSV data
sales = LOAD '/ecommerce/input/online_retail_clean.csv'
USING PigStorage(',')
AS (
    InvoiceNo:chararray,
    StockCode:chararray,
    Description:chararray,
    Quantity:int,
    InvoiceDate:chararray,
    UnitPrice:double,
    CustomerID:chararray,
    Country:chararray,
    TotalPrice:double
);

-- Remove header / invalid rows
clean_sales = FILTER sales BY InvoiceNo != 'InvoiceNo'
    AND Quantity IS NOT NULL
    AND TotalPrice IS NOT NULL
    AND Country IS NOT NULL;

-- ============================================
-- 1. Total Revenue
-- ============================================

total_revenue = GROUP clean_sales ALL;

total_revenue_result = FOREACH total_revenue
    GENERATE SUM(clean_sales.TotalPrice) AS Total_Revenue;

STORE total_revenue_result
INTO '/ecommerce/pig/total_revenue'
USING PigStorage(',');

-- ============================================
-- 2. Revenue by Country
-- ============================================

country_group = GROUP clean_sales BY Country;

country_revenue = FOREACH country_group
    GENERATE group AS Country,
    SUM(clean_sales.TotalPrice) AS Revenue;

STORE country_revenue
INTO '/ecommerce/pig/country_revenue'
USING PigStorage(',');

-- ============================================
-- 3. Quantity by Country
-- ============================================

country_quantity = FOREACH country_group
    GENERATE group AS Country,
    SUM(clean_sales.Quantity) AS Total_Quantity;

STORE country_quantity
INTO '/ecommerce/pig/country_quantity'
USING PigStorage(',');

-- ============================================
-- 4. Average Unit Price by Country
-- ============================================

country_avg_price = FOREACH country_group
    GENERATE group AS Country,
    AVG(clean_sales.UnitPrice) AS Average_Unit_Price;

STORE country_avg_price
INTO '/ecommerce/pig/country_avg_price'
USING PigStorage(',');

-- ============================================
-- 5. Total Quantity Sold
-- ============================================

total_quantity_group = GROUP clean_sales ALL;

total_quantity_result = FOREACH total_quantity_group
    GENERATE SUM(clean_sales.Quantity) AS Total_Quantity_Sold;

STORE total_quantity_result
INTO '/ecommerce/pig/total_quantity'
USING PigStorage(',');