import vdiff; from vdiff import *

if __name__ == '__main__':
    print 'Calculating the RMSE metric..'     
    '''obtain the RMSE metric for the case of intnode_CLFT - endnode'''
    #ms = dss_ts_diff_metric('D:\delta\dsm2_v8\studies\Delta_Corridor_NT\output\Delta_Corridor_NT_out.dss','D:\delta\dsm2_v8\studies\Delta_Corridor_NT\output\DC_Intnode_CLFT.dss',rmse,'01SEP1974 0000 - 01SEP1991 0000','FLOW','y','D:\delta\dsm2_v8\studies\Delta_Corridor_NT\output\IntCLFT-End_FLOW.dss')
    #get_metric_xy(ms,'D:/delta/dsm2_v8/studies/Delta_Corridor_NT/output/hydro_echo_Delta_Corridor_NT.inp','D:/delta/dsm2_v8/studies/Delta_Corridor_NT/output/NT_DC_gis.inp','D:/delta/dsm2_v8/studies/Delta_Corridor_NT/output/xyz_IntCLFT_End_FLOW.txt')
    #ms = dss_ts_diff_metric('D:\delta\dsm2_v8\studies\Delta_Corridor_NT\output\Delta_Corridor_NT_out.dss','D:\delta\dsm2_v8\studies\Delta_Corridor_NT\output\DC_Intnode_CLFT.dss',rmse,'01SEP1974 0000 - 01SEP1991 0000','EC','y','D:\delta\dsm2_v8\studies\Delta_Corridor_NT\output\IntCLFT-End_EC.dss')
    #get_metric_xy(ms,'D:/delta/dsm2_v8/studies/Delta_Corridor_NT/output/hydro_echo_Delta_Corridor_NT.inp','D:/delta/dsm2_v8/studies/Delta_Corridor_NT/output/NT_DC_gis.inp','D:/delta/dsm2_v8/studies/Delta_Corridor_NT/output/xyz_IntCLFT_End_EC.txt')
                            
    #ms = dss_ts_diff_metric('D:\delta\dsm2_v8\studies\Delta_Corridor_NT\output\Delta_Corridor_NT_out.dss','D:\delta\dsm2_v8\studies\Delta_Corridor_NT\output\DC_Intnode_CLFT.dss',rmse,'01SEP1974 0000 - 01SEP1991 0000','STAGE','y','D:\delta\dsm2_v8\studies\Delta_Corridor_NT\output\IntCLFT-End_STAGE.dss')
    #get_metric_xy(ms,'D:/delta/dsm2_v8/studies/Delta_Corridor_NT/output/hydro_echo_Delta_Corridor_NT.inp','D:/delta/dsm2_v8/studies/Delta_Corridor_NT/output/NT_DC_gis.inp','D:/delta/dsm2_v8/studies/Delta_Corridor_NT/output/xyz_IntCLFT_End_STAGE.txt')
    '''obtain the RMSE metric for the case of intnode_SUS9 - endnode'''
    ms = dss_ts_diff_metric('D:\delta\dsm2_v8\studies\Delta_Corridor_NT\output\Delta_Corridor_NT_out.dss','D:\delta\dsm2_v8\studies\Delta_Corridor_NT\output\DC_Intnode_SUS9_CLFT.dss',rmse,'01SEP1974 0000 - 01SEP1991 0000','FLOW','y','D:\delta\dsm2_v8\studies\Delta_Corridor_NT\output\IntSUS9-End_FLOW.dss')
    get_metric_xy(ms,'D:/delta/dsm2_v8/studies/Delta_Corridor_NT/output/hydro_echo_Delta_Corridor_NT.inp','D:/delta/dsm2_v8/studies/Delta_Corridor_NT/output/NT_DC_gis.inp','D:/delta/dsm2_v8/studies/Delta_Corridor_NT/output/xyz_IntSUS9_End_FLOW.txt')
    ms = dss_ts_diff_metric('D:\delta\dsm2_v8\studies\Delta_Corridor_NT\output\Delta_Corridor_NT_out.dss','D:\delta\dsm2_v8\studies\Delta_Corridor_NT\output\DC_Intnode_SUS9_CLFT.dss',rmse,'01SEP1974 0000 - 01SEP1991 0000','EC','y','D:\delta\dsm2_v8\studies\Delta_Corridor_NT\output\IntSUS9-End_EC.dss')
    get_metric_xy(ms,'D:/delta/dsm2_v8/studies/Delta_Corridor_NT/output/hydro_echo_Delta_Corridor_NT.inp','D:/delta/dsm2_v8/studies/Delta_Corridor_NT/output/NT_DC_gis.inp','D:/delta/dsm2_v8/studies/Delta_Corridor_NT/output/xyz_IntSUS9_End_EC.txt')
    ms = dss_ts_diff_metric('D:\delta\dsm2_v8\studies\Delta_Corridor_NT\output\Delta_Corridor_NT_out.dss','D:\delta\dsm2_v8\studies\Delta_Corridor_NT\output\DC_Intnode_SUS9_CLFT.dss',rmse,'01SEP1974 0000 - 01SEP1991 0000','STAGE','y','D:\delta\dsm2_v8\studies\Delta_Corridor_NT\output\IntSUS9-End_STAGE.dss')
    get_metric_xy(ms,'D:/delta/dsm2_v8/studies/Delta_Corridor_NT/output/hydro_echo_Delta_Corridor_NT.inp','D:/delta/dsm2_v8/studies/Delta_Corridor_NT/output/NT_DC_gis.inp','D:/delta/dsm2_v8/studies/Delta_Corridor_NT/output/xyz_IntSUS9_End_STAGE.txt')
    print 'Done!'        