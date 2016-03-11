#!/bin/bash
set -v

fail() {
  echo "Failed Output #$1"
  echo "  Data: ${DATA}"
  echo "  Expected: ${EXPECTED}"
  echo "  Received: ${OUTPUT}"
  echo "  Return Code: ${RETURN}"
  exit 1
}

# Test #1 - No Data
EXPECTED='{"error": "Could not serve request (POST contained no data)"}'
DATA=""
OUTPUT=$(curl -XPOST 'http://199.116.235.225:8000/traffic' -d "${DATA}")
RETURN=$?
if [ "${OUTPUT}" != "${EXPECTED}" ]; then
  fail '1 - No Data'
fi

# Test #2a - Invalid Data (Not JSON)
EXPECTED='{"error": "Could not serve request (Could not parse POST data into traffic data JSON)"}'
DATA='There was like.... around 20 cars this hour, yo.'
OUTPUT=$(curl -XPOST 'http://199.116.235.225:8000/traffic' -d "${DATA}")
RETURN=$?
if [ "${OUTPUT}" != "${EXPECTED}" ]; then
  fail '2a - Invalid Data (Not JSON)'
fi

# Test #2b - Invalid Data (Incorrect JSON)
EXPECTED='{"error": "Could not serve request (Could not parse POST data into traffic data JSON)"}'
DATA='{"key":"message", "value":"There was like.... around 20 cars this hour, yo.","timestamp":1457668800}'
OUTPUT=$(curl -XPOST 'http://199.116.235.225:8000/traffic' -d "${DATA}")
RETURN=$?
if [ "${OUTPUT}" != "${EXPECTED}" ]; then
  fail '2b - Invalid Data (Incorrect JSON)'
fi

# Test #3 - Key has tilda
EXPECTED=' {"error": "Could not serve request (Invalid traffic data posted)"}'
DATA='{"from":{"lat":53.4635928,"lon":-113.501307},"to":{"lat":53.4635554,"lon":-113.5017748},"key":"CARS~PER~HOUR","value":"2.0","timestamp":1457668800}'
OUTPUT=$(curl -XPOST 'http://199.116.235.225:8000/traffic' -d "${DATA}")
RETURN=$?
if [ "${OUTPUT}" != "${EXPECTED}" ]; then
  fail '3 - Key has tilda'
fi

# Test #4 - Lat & long out of range
EXPECTED='{"error": "Could not serve request (Could not parse POST data into traffic data JSON)"}'
DATA="{from\":{\"lat\":653.4635928,\"lon\":-3113.501307},\"to\":{\"lat\":553.4635554,\"lon\":-1113.5017748},\"key\":\"CARS_PER_HOUR\",\"value\":2.0,\"timestamp\":1457668800}"
OUTPUT=$(curl -XPOST 'http://199.116.235.225:8000/traffic' -d "${DATA}")
RETURN=$?
if [ "${OUTPUT}" != "${EXPECTED}" ]; then
  fail '4 - Lat & Long out of range'
fi

# Test #5 - Valid-ish data
EXPECTED='test'
DATA='{"from":{"lat":53.4635928,"lon":-113.501307},"to":{"lat":53.4635554,"lon":-113.5017748},"key":"CARS_PER_HOUR","value":"2.0","timestamp":1457668800}'
OUTPUT=$(curl -XPOST 'http://199.116.235.225:8000/traffic' -d "${DATA}")
RETURN=$?
if [ "${OUTPUT}" != "${EXPECTED}" ]; then
  fail '5 - Valid-ish data'
fi
