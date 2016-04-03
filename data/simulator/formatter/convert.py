import csv
import requests

# data.csv was made by downloading the following data:
#  https://www.dropbox.com/s/cjzxwsr6z73wxab/EdmontonTrafficDB%20%281%29.7z?dl=0
# and running the following query:
#   SELECT
#     S.latitude,
#     S.longitude,
#     T.direction,
#     DATEDIFF("s", '1 Jan 1970', T.event_date_time) AS ts,
#     T.count
#   FROM
#   Sites S
#     INNER JOIN
#       TrafficEvents T
#     ON
#       S.site_id = T.site_id
#   WHERE
#     S.latitude IS NOT NULL
#   ORDER BY
#     S.latitude,
#     S.longitude,
#     T.direction,
#     DATEDIFF("s", '1 Jan 1970', T.event_date_time)
with open('data.csv', 'r') as data:
    reader = csv.DictReader(data)
    last_lat = None
    last_lon = None
    segments = []
    from_geohash = None
    for row in reader:
        # Data from CSV
        latitude = float(row["latitude"])
        longitude = float(row["longitude"])
        direction = row["direction"]
        timestamp = int(row["ts"])
        count = int(row["count"])

        if last_lon != longitude or last_lat != latitude:
            # Get & construct connected segments from REST API
            response = requests.get("http://199.116.235.225:8000/segment?lat={0}&lon={1}".format(latitude, longitude))
            json = response.json()
            segments = []
            for key, value in json.items():
                if key == "from":
                    from_geohash = value
                else:
                    segment = {
                        "location": key
                    }
                    for key2, value2 in value.items():
                        if key2 == "highway" or key2 == "name":
                            segment[key2] = value2

                print("Key: {0} && Value: {1}".format(key, value))

            last_lat = latitude
            last_lon = longitude