import string, re, os, sys, math, glob
from vtimeseries import *
from vista.set import *
from vista.db.dss import *
from vutils import *
from gov.ca.dsm2.input.parser import Parser
from gov.ca.dsm2.input.parser import Tables
from gov.ca.dsm2.input.model import *

if __name__ == "__main__":

    # percent change for perturbed values
    pctChange = 20.
    ## just one of the below should be True, the others False
    # channel input parameters
    MANN = True
    DISP = False
    XELEV = False
    XTOPW = False
    LENGTH = False
    # node input parameters
    DICU = False
    # reservoir input parameters
    RESDEPTH = False

    infile = 'd:/delta/models/studies/2000-Calibration/historical/output/hydro_echo_hist-calib2000.inp'
    DICUdir = 'd:/delta/models/timeseries/'
    outdir = 'd:/delta/models/studies/2010-Calibration/SensitivityTests/PerturbedInputFiles/'
    outfilenm = 'PerturbedInp'
    
    p = Parser()
    tables = p.parseModel(infile)
    for fn in glob.glob(outdir+'???'):
        try: os.remove(fn)
        except: pass
    if MANN or DISP or XELEV or XTOPW or LENGTH:
        channels = tables.toChannels()
        for chan in channels.getChannels():
            chan3 = "%03d" % int(chan.getId())
            if MANN:    # mannings N...
                PTBID = 'ManN_Ch' + chan3
                Val = chan.getMannings()
                newVal = int(10000. * (Val * (1. + pctChange / 100.)) + 0.5) / 10000.
                chan.setMannings(newVal)
            if LENGTH:    # nominal channel length...
                PTBID = 'Len_Ch' + chan3
                Val = chan.getLength()
                newVal = int(10000. * (Val * (1. + pctChange / 100.)) + 0.5) / 10000.
                chan.setLength(int(newVal))
            elif DISP:  # dispersion coefficient
                PTBID = 'Disp_Ch' + chan3
                Val = chan.getDispersion()
                newVal = int(10000. * (Val * (1. + pctChange / 100.)) + 0.5) / 10000.
                chan.setDispersion(newVal)
            elif XELEV or XTOPW: # cross sections in channel
                if XELEV: PTBID = 'XElev_Ch' + chan3
                if XTOPW: PTBID = 'XTopW_Ch' + chan3
                for xs in chan.getXsections():
                    for lyr in xs.getLayers():
                        area = lyr.getArea()
                        elev = lyr.getElevation()
                        TW = lyr.getTopWidth()
                        WP = lyr.getWettedPerimeter()
                        if XELEV: lyr.setElevation(int(100. * (elev * (1. + pctChange / 100.) + 0.5)) / 100.)
                        if area == 0:
                            continue
                        if XTOPW: lyr.setTopWidth(int(1000. * (TW * (1. + pctChange / 100.) + 0.5)) / 1000.)
                        lyr.setArea(int(1000. * (area * (1. + pctChange / 100.) + 0.5)) / 1000.)
                        lyr.setWettedPerimeter(int(1000. * (WP * (1. + pctChange / 100.)) + 0.5) / 1000.)
            newChans = Channels()
            newChans.addChannel(chan)
            newTables = Tables().fromChannels(newChans)
            outfile = outdir + outfilenm + PTBID + '.inp'
            fid_NewVal = open(outfile, 'w')
            fid_EnvLabel = open(outdir + chan3, 'w')
            fid_EnvLabel.write('ENVVAR\n')
            fid_EnvLabel.write('NAME\tVALUE\n'.expandtabs())
            fid_EnvLabel.write(('PTB\t' + PTBID).expandtabs())
            fid_EnvLabel.write('\nEND\n\n')
            fid_EnvLabel.close()
            for i in range(2):
                fid_NewVal.write(newTables[i].toStringRepresentation().expandtabs())
            fid_NewVal.close()
        # end channel loop
        print 'Prepared', len(channels.getChannels()), 'Channel files'
    elif RESDEPTH:
        reservoirs = tables.toReservoirs()
        for res in reservoirs.getReservoirs():
            resname = res.getName()
            PTBID = 'Depth_Res' + resname
            Val = res.getArea()
            newVal = int(10000. * (Val * (1. + pctChange / 100.)) + 0.5) / 10000.
            res.setArea(newVal)
            newRes = Reservoirs()
            newRes.addReservoir(res)
            newTables = Tables().fromReservoirs(newRes)
            outfile = outdir + outfilenm + PTBID + '.inp'
            fid_EnvLabel = open(outdir + resname, 'w')
            fid_EnvLabel.write('ENVVAR\n')
            fid_EnvLabel.write('NAME\tVALUE\n'.expandtabs())
            fid_EnvLabel.write(('PTB\t' + PTBID).expandtabs())
            fid_EnvLabel.write('\nEND\n\n')
            fid_EnvLabel.close()
            fid_NewVal = open(outfile, 'w')
            for i in range(2):  
                fid_NewVal.write(newTables[i].toStringRepresentation().expandtabs())
            fid_NewVal.close()
        # end reservoir loop
        print 'Prepared', len(reservoirs.getReservoirs()), 'Reservoir files'
    elif DICU:
        # Set one to True, the others to False (Diversions, Return Flows, EC of return flows)
        QDIV = False
        QRET = False
        ECRET = True
        # Perturbed values for flows could be either a single replacement value, 
        # or an incremental (additional) value.
        # Use incremental value for each node to avoid huge directory
        # of full input for each nodal perturbation.
        # For ECs of return flows, use a replacement value
        if QDIV: 
            QType = 'QDIV'
            Sign = '-1'
        if QRET: 
            QType = 'QRET'
            Sign = '+1'
        if ECRET: QType = 'ECRET'
        if QDIV or QRET: DICUfile = DICUdir + 'dicu_200705.dss'
        if ECRET: DICUfile = DICUdir + 'dicuwq_200611_expand.dss'
        dss_group = opendss(DICUfile)
        if ECRET: dss_group.filterBy('/DRAIN-EC/')
        count = 0
        for dataref in dss_group.getAllDataReferences():
            dataset = dataref.getData()
            inpath = dataref.getPathname()
            inpath.setPart(inpath.F_PART,'PRTB-'+QType)
            B = inpath.getPart(inpath.B_PART)
            C = inpath.getPart(inpath.C_PART)
            if not re.search('^[0-9]+$',B):    # nodes only
                continue
            node3 = "%03d" % int(B)
            update = False  # flag that this path is updated
            if (QDIV and inpath.getPart(inpath.C_PART)=='DIV-FLOW') or \
               (QRET and inpath.getPart(inpath.C_PART)=='DRAIN-FLOW'):
                update = True
                dataref = dataref * pctChange/100.
            if ECRET and inpath.getPart(inpath.C_PART)=='DRAIN-EC':
                update = True
                dataref = dataref * (1.+pctChange/100.)
            if not update:
                continue
            dataset = dataref.getData()
            PTBID = 'DICU-' +QType + '_Nd' + node3
            # create file for environment variable study label
            fid_EnvLabel = open(outdir + node3, 'w')
            fid_EnvLabel.write('ENVVAR\n')
            fid_EnvLabel.write('NAME\tVALUE\n'.expandtabs())
            fid_EnvLabel.write(('PTB\t' + PTBID).expandtabs())
            fid_EnvLabel.write('\nEND\n')
            fid_EnvLabel.close()
            # create file to read in updated value
            dicufile = outdir + outfilenm + PTBID + '.dss'
            outfile = outdir + outfilenm + PTBID + '.inp'
            fid_NewVal = open(outfile, 'w')
            if QDIV or QRET:
                fid_NewVal.write('SOURCE_FLOW\n')
                fid_NewVal.write('NAME\t\tNODE\tSIGN\tFILLIN\tFILE\t\tPATH\n'.expandtabs())
                if inpath.getPart(inpath.C_PART)=='DIV-FLOW': name = 'dicu_div_'
                if inpath.getPart(inpath.C_PART)=='DRAIN-FLOW': name = 'dicu_drain_'
                if inpath.getPart(inpath.C_PART)=='SEEP-FLOW': name = 'dicu_seep_'
                fid_NewVal.write(name+B+'\t'+B+'\t'+Sign+'\t\tlast\t'+ \
                         os.path.basename(dicufile)+'\t'+ \
                         inpath.getFullPath()+'\n'.expandtabs())
            if ECRET:
                fid_NewVal.write('NODE_CONCENTRATION\n')
                fid_NewVal.write('NAME\t\tNODE_NO\tVARIABLE\tFILLIN\tFILE\t\tPATH\n'.expandtabs())
                fid_NewVal.write('dicu_drain_'+B+'\t'+B+'\tEC\t\tlast\t'+ \
                         os.path.basename(dicufile)+'\t'+ \
                         inpath.getFullPath()+'\n'.expandtabs())                
            fid_NewVal.write('END\n')
            fid_NewVal.close()
            # write updated value to new DSS file
            writedss(dicufile,inpath.getFullPath(),dataset)
            count+=1
        print 'Prepared', count, 'DICU DSS paths'
        #
    else:
        raise 'Set either MANN or DISP or LENGTH or XSECT or DICU True.'
    #
    sys.exit()
