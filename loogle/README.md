# Loogle - Your Local Google

Index and search your local hard drive

Indexes all pdf, djvu, txt, epub files under a given directory, saves
the index, and allows search on these indexed documents. Allows
updates, and deletes of files already indexed.

Point to any directory, specify an index database name, loogle will
index all books, documents for you. Content extraction is done through
textract which can extract files from pretty much all common file
formats.

Loogle can detect additions, removals. TBD: file size changes
triggering incremental updates.

Requirements

whoosh

pandas

