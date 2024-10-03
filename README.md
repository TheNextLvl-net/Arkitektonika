# Arkitektonika

<p>
    <img src="https://raw.githubusercontent.com/IntellectualSites/Assets/main/standalone/Arkitektonika/Arkitektonika.png" width="150">
</p>

---

The original resource by IntellectualSites can be found [here](https://github.com/IntellectualSites/Arkitektonika)

Arkitektonika is a REST repository for NBT data. It accepts uploads of valid NBT data and stores them in a local folder
while accounting for its metadata in a local sqlite database. ~Optionally, uploaded files can be expired based on the
configurable age by running the prune script~. Files can always be deleted via their deletion key.

## To Run

### With Docker

```sh
git clone https://github.com/TheNextLvl-net/Arkitektonika.git &&
cd Arkitektonika &&
docker compose up
```

### From scratch

```sh
git clone https://github.com/TheNextLvl-net/Arkitektonika.git &&
cd Arkitektonika &&
./gradlew shadowJar &&
java -jar $(find build/libs/ -name "*.jar" -print -quit)
```

## Configuration

```json
{
  "port": 3000,
  "prune": 1800000,
  "maxSchematicSize": 1000000,
  "allowedOrigin": "*",
  "limiter": {
    "windowMs": 60000,
    "delayAfter": 30,
    "delayMs": 500
  }
}
```

> [!WARNING]
> the `limiter` is not implemented yet

| Config Key         | Description                                                                                                                |
|--------------------|----------------------------------------------------------------------------------------------------------------------------|
| port               | on which port should the application bind                                                                                  |
| prune              | defines how old records must be to be deleted by the prune script (in ms)                                                  |
| maxSchematicSize   | maximum size of schematic files to be accepted (in bytes)                                                                  |
| limiter.windowMs   | the frame of the limiter (after what duration should the limit gets reset)                                                 |
| limiter.delayAfter | After how many requests during windowMs should delayMs be applied                                                          |
| limiter.delayMs    | How many ms should the request take longer. Formula: `currentRequestDelay = (currentRequestAmount - delayAfter) * delayMs` |

## File structure:

```
data
├── config.json
└── database.db
```

`config.json` holds the user configuration data <br>
`database.db` holds the required data for each schematic <br>
~~`schematics`  holds all schematic file data~~

### Routes

All routes will be available at the exposed port (e.g. `localhost:3000`).

### Get files as base64

**GET `INSTANCE_URL/base64/{download_key}`**: get the file bytes as base64 encoded string

```sh
curl --request GET 'http://localhost:3000/base64/db6186c8795740379d26fc61ecba1a24'
```
response:

| Code | Meaning                            |
|------|------------------------------------|
| 200  | Success                            |
| 404  | File was not found in the database |
| 500  | A server error occurred            |

response body: `base64 string`

### Get file size

**GET `INSTANCE_URL/size/{download_key}`**: get the file size

```sh
curl --request GET 'http://localhost:3000/size/db6186c8795740379d26fc61ecba1a24'
```
response:

| Code | Meaning                            |
|------|------------------------------------|
| 200  | Success                            |
| 404  | File was not found in the database |
| 500  | A server error occurred            |

success body: `43554` (in bytes)

### Get file expiration

**GET `INSTANCE_URL/expiration/{download_key}`**: get the file expiration date

```sh
curl --request GET 'http://localhost:3000/expiration/db6186c8795740379d26fc61ecba1a24'
```

response:

| Code | Meaning                            |
|------|------------------------------------|
| 200  | Success                            |
| 404  | File was not found in the database |
| 500  | A server error occurred            |

success body: `2717940582741` (in millis)

### Set file expiration

**PUT `INSTANCE_URL/expiration/{delete_key}/{expiration}`**: set the file expiration date

```sh
curl --request PUT 'http://localhost:3000/expiration/11561161dffe4a1298992ce063be5ff9/2717940582741'
```

response:

| Code | Meaning                            |
|------|------------------------------------|
| 200  | Expiration updated                 |
| 404  | File was not found in the database |
| 500  | A server error occurred            |

### Upload a file

**POST `INSTANCE_URL/upload`**: send your file as multipart/form-data; example:

```sh
curl --request POST 'http://localhost:3000/upload' --form 'schematic=@/path/to/plot.schem'
```

response:

| Code | Meaning                                       |
|------|-----------------------------------------------|
| 200  | File was of valid NBT format and was accepted |
| 400  | File was not of valid NBT format              |
| 413  | File payload was too large and rejected       |
| 500  | Upload failed                                 |

success body:

```json
{
  "download_key": "db6186c8795740379d26fc61ecba1a24",
  "delete_key": "11561161dffe4a1298992ce063be5ff9",
  "expiration_date": 1717940582741
}
```

The download key allows you to download the file, and the delete key lets you delete it. Share the `download_key`, but
not the `delete_key`.

### Rename a file

**PUT `INSTANCE_URL/rename/{deletion_key}/{name}`**: rename a file with the given `deletion_key` ; example:

```sh
curl --request PUT 'http://localhost:3000/rename/11561161dffe4a1298992ce063be5ff9/renamed-plot.schem'
```

response:

| Code | Meaning                            |
|------|------------------------------------|
| 200  | File was successfully renamed      |
| 404  | File was not found in the database |

### Download a file

**GET `INSTANCE_URL/download/{download_key}`**: download a file with the given `download_key`; example:

```sh
curl --request GET 'http://localhost:3000/download/db6186c8795740379d26fc61ecba1a24'
```

The response for this is in the form of status codes only.

| Code | Meaning                                                                                |
|------|----------------------------------------------------------------------------------------|
| 200  | File was found, prospective download would succeed                                     |
| 404  | File was not found in the database                                                     |
| 410  | File metadata is in accounting table, but file is not on disk or already expired       |
| 500  | An internal server error occurred due to corrupted metadata (missing data in database) |

On success, the file is sent as an attachment for download to the browser / requester.

### Delete a file

**DELETE `PUBLIC_URL/delete/{delete_key}`**: delete a file with the given `delete_key`; example:

```sh
curl --request DELETE 'http://localhost:3000/delete/11561161dffe4a1298992ce063be5ff9'
```

The response for this is in the form of status codes only.

| Code | Meaning                                                                                |
|------|----------------------------------------------------------------------------------------|
| 200  | File was found, prospective deletion would succeed                                     |
| 404  | File was not found in the database                                                     |
| 410  | File metadata is in accounting table, but file is not on disk or already expired       |
| 500  | An internal server error occurred due to corrupted metadata (missing data in database) |

On success, the file is deleted and the record is marked as expired in the database. 
