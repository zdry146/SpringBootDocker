use sample_analytics;

show tables;

db.accounts.find().pretty();

db.accounts.find({products: {$all: ["Commodity", "InvestmentStock"]}}).pretty();

db.accounts.find({account_id: 443178, products: "Commodity"}).pretty();

db.accounts.find({limit: 10000, products: {$in: ["Commodity", "Brokerage"]}}).pretty();

print(db.accounts.updateOne({account_id: 443178}, {$addToSet: {products: {$each: ["CurrencyService", "Brokerage", "Commodity", "InvestmentStock"]}}}));

print(db.accounts.updateOne({account_id: 443178}, {$push: {products: {$each: [], $sort: 1}}}));

print(db.accounts.updateOne({account_id: 443178}, {$push: {products: {$each: ["CurrencyService", "Brokerage"], $sort: 1}}}));

print(db.accounts.updateOne({account_id: 443178}, {$pullAll: {products: ["CurrencyService", "Brokerage"]}}));

print(db.accounts.updateOne({account_id: 443178}, {$pop: {products: 1}}));

print(db.accounts.updateOne({account_id: 443178}, {$unset: {sort: -1}}));
