# Background
X2 compliance rules within CALSIM use ANNs for calculating the X2. This ANN is trained using DSM2 output.

The script that calculates X2 for DSM2 DSS output is in this github repository: https://github.com/CADWRDeltaModeling/dsm2-vista/tree/master/vista/scripts/x2/

The X2 location is calculated from the EC along channels:
  In the Sacramento River, from Martinez to Rio Vista
  In the San Joaquin River, from Martinez to the North Fork of the Mokelumne River, just upstream of San Andreas Landing.

You must have a DSS file containing DSM2 Qual EC output. Qual must have been run using the output specification file in this repository: output_channel_ec_for_x2.inp.

# Installation
The script x2_daily_v3.py is run using vscript, which is an extension of python created by Nicky Sandhu for the Vista application, which was included with earlier versions of DSM2. 
Here is a link to one of the earlier versions of DSM2 which includes Vista: https://data.cnra.ca.gov/dataset/dsm2/resource/8828c95e-7ea0-406d-bf01-6e7d800d94f8

# Usage
(path to vscript)\vscript x2_daily_v3.py (path to DSS EC output file) (path to output DSS file) (output DSS F part) (time window) ("sac" or "sjr")

# example
d:\delta\dsm2_V8.2\vista\bin\vscript calcx2.py d:/delta/2023HistoricalUpdate/studies/historical/output/hist_v2023_01.dss x2.dss base_study "01Jan1990 0000 - 31Dec2021 0000" sac

# output
X2 is calculated as a time series, and the results are written to a DSS file, using the information provided to the script.

