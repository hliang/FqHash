# FqHash

FqHash is a GUI tool to check the integrity of sequence data. It can:
+ Generate MD5 checksum for input files (any format)
+ Count number of sequences in fastq file
+ Verify the original checksum, and highlight any mismatch

![FqHash Screenshot](/src/io/github/hliang/FqHash/Resources/FqHash.png)

## Installation
<em>Requirement: Java Runtime Environment (JRE) 8+</em>

Download the latest jar file from the [release page](https://github.com/hliang/FqHash/releases) to your computer, and simply double-click it.

## Using FqHash
1. Add files/folder. If a folder is chosen, all sequence files inside will be added to the table.
2. Select "Count Sequences" if needed.
3. Click "Analyze" to start processing.
4. After calculation is done, enter original MD5 checksum values to verify them.
