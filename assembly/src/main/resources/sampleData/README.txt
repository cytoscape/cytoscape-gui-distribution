README.txt: Updated on 10/15/2015

	This directory contains sample data for Cytoscape 3. It includes the
	following data sets:

----------------------------------------------------------------------------

<Network Files>
galFiltered.sif
galFiltered.gml
galFiltered.xgmml
galFiltered.csv
galFiltered.graphml

<Table Data Files>
galExpData.csv

<Session File>
galFiltered.cys

These files contain an interaction network and expression data from
the paper:

Ideker, T., Thorsson V, Ranish JA, Christmas R, Buhler J, Eng JK, Bumgarner R,
Goodlett DR, Aebersold R, Hood L. Integrated genomic and proteomic analyses of
a systematically perturbed metabolic network. Science, 292, 929-34 (2001).

The 'galExpData.csv' file contains 20 experimental conditions involving
systematic perturbations of the galactose utilization pathway in yeast.
The 'galFiltered.*' files contain an interaction network of 331 genes that
were significantly differentially expressed in at least one of the 20
experimental conditions. The two network files are identical networks in
different formats; the '.sif' is in the simple interactions format,
the '.gml' is in GML format, and '.xgmml' is in XGMML format.

'galFiltered.csv' is a standard text file version of the network + table data.
You can import this from 'import from table' menu. 

The 'galFiltered.cys' is a CYtoscape Session file contains network data, 
attributes, and desktop states.

----------------------------------------------------------------------------

BINDyeast.sif

This is a Cytoscape network containing all of the yeast interactions known
to the BIND database (http://www.bind.ca) circa 10/10/2006.

----------------------------------------------------------------------------

BINDhuman.sif

This is a Cytoscape network containing all of the human interactions known
to the BIND database (http://www.bind.ca) circa 10/10/2006.

----------------------------------------------------------------------------

yeastHighQuality.sif

This is a Cytoscape network combining two public data sets of high quality
interactions in yeast. It includes:

2455 high-confidence protein-protein interactions from the paper

von Mering C, Krause R, Snel B, Cornell M, Oliver SG, Fields S, Bork P.
Comparative assessment of large-scale data sets of protein-protein
interactions. Nature, 417, 399-403, May 2002.

and 4433 high confidence protein-dna interactions (p-values < 0.001) from
the paper

Lee, T.I., Rinaldi, N.J., Robert, F., Odom, D.T., Bar-Joseph, Z., Gerber, G.K.,
Hannett, N.M., Harbison, C.R., Thompson, C.M., Simon I., Zeitlinger J.,
Jennings, E.G., Murray, H.L., Gordon, D.B., Ren, B., Wyrick, J.J., Tagne, J.,
Volkert T.L., Fraenkel, E., Gifford D.K., and Young, R.A.
Transcriptional regulatory networks in Saccharomyces cerevisiae.
Science, 298: 799-804 (2002).
http://web.wi.mit.edu/young/regulator_network/

----------------------------------------------------------------------------

nestedNetworks.nnf

Nested Network File (NNF) format is a simple text format to represent 
networks with nested structure.  This sample file contains multiple networks 
with nested nodes (nodes contain reference to other network).

----------------------------------------------------------------------------

BIOGRID-ORGANISM-Arabidopsis_thaliana_Columbia-3.4.129.mitab
BIOGRID-ORGANISM-Caenorhabditis_elegans-3.4.129.mitab
BIOGRID-ORGANISM-Danio_rerio-3.4.129.mitab
BIOGRID-ORGANISM-Drosophila_melanogaster-3.4.129.mitab
BIOGRID-ORGANISM-Escherichia_coli_K12_MG1655-3.4.129.mitab
BIOGRID-ORGANISM-Homo_sapiens-3.4.129.mitab
BIOGRID-ORGANISM-Mus_musculus-3.4.129.mitab
BIOGRID-ORGANISM-Saccharomyces_cerevisiae_S288c-3.4.129.mitab

PSI-MI Tab 2.5 file format is a standard text representation of interaction data.
Cytoscape 3 recognizes files end with '.mitab' as PSI-MI Tab file.  The sample 
data is compiled by the BioGRID project: 

Stark C, Breitkreutz BJ, Reguly T, Boucher L, Breitkreutz A, Tyers M. 
Biogrid: A General Repository for Interaction Datasets. 
Nucleic Acids Res. Jan1; 34:D535-9
 
----------------------------------------------------------------------------

DNA_Damage_Bypass_Reactome_73893.owl

BioPAX Level 2 and 3 files are supported by Cytoscape.  This sample file is 
created by Reactome project:

http://www.reactome.org/
