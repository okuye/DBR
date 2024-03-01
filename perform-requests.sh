#!/bin/bash

echo "Performing POST /transaction for an existing account with sufficient funds..."
curl -X POST -H "Content-Type: application/json" \
    -d '{"accountId":"123", "amount":100.0, "description":"Test Transaction"}' \
    http://localhost:8080/transaction

echo "Performing POST /transaction for a non-existing account..."
curl -X POST -H "Content-Type: application/json" \
    -d '{"accountId":"non-existing", "amount":100.0, "description":"Test Transaction"}' \
    http://localhost:8080/transaction

echo "Performing POST /transaction with insufficient funds..."
curl -X POST -H "Content-Type: application/json" \
    -d '{"accountId":"123", "amount":10000.0, "description":"Large Transaction"}' \
    http://localhost:8080/transaction

echo "Performing GET /transaction/history/123 to return transaction history for an existing account..."
curl http://localhost:8080/transaction/history/123

echo "Performing GET /transaction/history/non-existing to return 404 for non-existing account..."
curl http://localhost:8080/transaction/history/non-existing

echo "Performing GET /account/123 to return 200 and account details..."
curl http://localhost:8080/account/123

echo "Performing GET /account/non-existing to return 404..."
curl http://localhost:8080/account/non-existing
