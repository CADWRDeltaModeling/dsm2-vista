package dsm2.server.hec.test;

import java.util.Vector;
import java.util.regex.Pattern;

import hec.heclib.dss.CondensedReference;
import hec.heclib.dss.HecDss;

public class TestDSSCatalogSearch {
	public static void main(String[] args) throws Exception {
		HecDss dss = HecDss.open("D:/delta/dsm2_v812_2309_fresh/studies/historical/output/historical_v81_qual.dss");
		Vector<CondensedReference> condensedCatalog = dss.getCondensedCatalog();
		String path = "//rsac054/";
		System.out.println("Original search: "+path+" ---> replaced by "+ replaceAndEscape(path));
		Pattern p = Pattern.compile(replaceAndEscape(path), Pattern.CASE_INSENSITIVE);
		for (CondensedReference condensedReference : condensedCatalog) {
			String pathname = condensedReference.getNominalPathname();
			if (p.matcher(pathname).matches()) {
				System.out.println("match for "+p+" is "+pathname);

			}
		}
		path = "/QUAL8.1.2/ROCK_SL_PP1/EC/01JAN1999 - 01MAR2012/15MIN/HISTORICAL_V81+FROM-ALL/";
		System.out.println("Original search: "+path+" ---> replaced by "+ replaceAndEscape(path));
		p = Pattern.compile(replaceAndEscape(path), Pattern.CASE_INSENSITIVE);
		for (CondensedReference condensedReference : condensedCatalog) {
			String pathname = condensedReference.getNominalPathname();
			if (p.matcher(pathname).matches()) {
				System.out.println("match for "+p+" is "+pathname);
			}
		}
		path = "//rock_sl_pp1////";
		System.out.println("Original search: "+path+" ---> replaced by "+ replaceAndEscape(path));
		p = Pattern.compile(replaceAndEscape(path), Pattern.CASE_INSENSITIVE);
		for (CondensedReference condensedReference : condensedCatalog) {
			String pathname = condensedReference.getNominalPathname();
			if (p.matcher(pathname).matches()) {
				System.out.println("match for "+p+" is "+pathname);
			}
		}
		path = "/.*/rock_sl_pp1/.*/.*/.*/.*/";
		System.out.println("Original search: "+path+" ---> replaced by "+ replaceAndEscape(path));
		p = Pattern.compile(replaceAndEscape(path), Pattern.CASE_INSENSITIVE);
		for (CondensedReference condensedReference : condensedCatalog) {
			String pathname = condensedReference.getNominalPathname();
			if (p.matcher(pathname).matches()) {
				System.out.println("match for "+p+" is "+pathname);
			}
		}

	}

	public static String replaceAndEscape(String dsspath) {
		String[] fields = dsspath.split("/");
		StringBuilder sb = new StringBuilder();
		int slashCount = 0;
		for (String f : fields) {
			slashCount++;
			if (slashCount == 1) {
				sb.append("/");
				continue;
			}
			if (f.length() == 0) {
				sb.append(".*");
			} else {
				sb.append(Pattern.quote(f));
			}
			sb.append("/");
		}
		while (slashCount < 7) {
			sb.append(".*/");
			slashCount++;
		}
		return sb.toString();
	}
}
