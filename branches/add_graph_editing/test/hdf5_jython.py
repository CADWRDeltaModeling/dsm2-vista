from vutils import *
from ncsa.hdf.object.h5 import H5File
h5file = H5File(r'D:\dev\workspace\vista-hdf5\test\hist.h5')
hydro = h5file.get("/hydro")
[ (e.name, e.value[0]) for e in hydro.metadata]
from vista.db.hdf5 import *
g=HDF5Group(r'D:\dev\workspace\vista-hdf5\test\hist.h5')
GroupFrame(g)