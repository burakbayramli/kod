# Loogle - Your Local Google

Index and search your local hard drive, a pure python replacement for
recoll.

Indexes all pdf, djvu, txt, epub files under a given directory, saves
the index, and allows search on these indexed documents. 

Point to any directory, specify an index database name, loogle will
index all books, documents for you. Content extraction is done through
`textract` which can extract files from pretty much all common file
formats. Loogle can detect additions, removals.

TODO: file size changes triggering incremental updates.

Requirements

Python packages

`textract`
`sqlite3`

Programs

`pdftotext`
`djvutxt`

Install with `apt-get install djulibre-bin  poppler-utils`



