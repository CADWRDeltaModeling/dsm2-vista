#
from vista.set import PartNamePredicate, PartSort, SortMechanism
#
g=opendss('/delta4/data/dss/IEP/hydro.dss')
# Let us first sort by the b part in increasing order and if b part is 
# the same order them in decreasing order with respect to the c part
p1 = PartNamePredicate(Pathname.C_PART, SortMechanism.DECREASING, None);
p2 = PartNamePredicate(Pathname.B_PART, SortMechanism.INCREASING, p1);
# create a sorter
sorter = PartSort(p2)
# sort the group using this sorter
g.sortBy(sorter)
