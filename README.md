# Contacts Importer
### A java program to import contacts from .csv file and save them in .vcf format in the disk.

A dummy file can be generated from the `Generate` menu. Input how many lines of csv you want and submit.
Upload a `.csv` file and submit. The file will be first uploaded in the server and then processed concurrently.
Number of the threads can be defined in the `Constants` file in the `THREAD_POOL_SIZE` field.
The `PhysicalStorageFileProcessingStrategy` class is an implementation of the `FileProcessingStrategy` interface.
The file processing strategy is hot swappable by any other strategy like `DataStorageFileProcessingStrategy` which will save the contacts in database.
The file upload and file processing logic is separated by an event publisher and listener.
Streams are used when downloading and uploading files so that it doesn't eat up memory. This enables working with large files.
Current implementation will create lots of files in the disk in the contacts directory. Beware of them!!
