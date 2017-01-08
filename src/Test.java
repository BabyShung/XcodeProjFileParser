import java.io.IOException;

import Classes.XcodeProjParser;
import Classes.ZHFilter;

public class Test {

	public static void main(String[] args) throws IOException {

		if (args == null || args.length == 0) {
			ZHFilter filter = new ZHFilter();
			String name = filter.findFileName(System.getProperty("user.dir"), ".xcodeproj");
			XcodeProjParser parser = new XcodeProjParser(name);
			parser.outputAllResourcesAndSourcesForEachTarget();
		}

		//TODO
//		if (args == null || args.length == 0) {
//			XcodeProjParser parser = new XcodeProjParser();
//			parser.outputAllResourcesAndSourcesForEachTarget();
//		} else if (args.length == 1) {
//			String str = args[0];
//			XcodeProjParser parser;
//			if (str.contains(".xcodeproj") || str.contains(".pbxproj")) {//Must cases
//				parser = new XcodeProjParser(str);
//				parser.compareBetweenTargetsInOneXCODEPROJ();
//			} else {
//				parser = new XcodeProjParser();
//				if (str.equals("1"))
//					parser.compareBetweenTargetsInOneXCODEPROJ();
//				else if (str.equals("2"))
//					parser.outputAllResourcesAndSourcesForEachTarget();
//				else
//					System.out
//							.println("\n\n\n--- wrong input, please try again. ---\n\n\n");
//			}
//		} else if (args.length == 2) {
//			String str1 = args[0];
//			String str2 = args[1];
//			XcodeProjParser parser;
//			if (str2.equals("YES")) {
//				parser = new XcodeProjParser("Alarm.xcodeproj", str1);
//				parser.compareBetweenTargetsInOneXCODEPROJ();
//				return;
//			}
//
//			if (str1.contains(".xcodeproj") && str2.contains(".xcodeproj")) {
//				parser = new XcodeProjParser();
//				parser.compareSameTargetsBetweenTwoXCODEPROJFiles(str1, str2);
//			} else {
//				System.out
//						.println("\n\n\n--- wrong input, please try again. ---\n\n\n");
//			}
//		}

		// XcodeProjParser pp = new XcodeProjParser("Alarm.xcodeproj");
		//
		// XcodeProjParser pp = new XcodeProjParser("project.pbxproj");

		// pp.compareBetweenTargetsInOneXCODEPROJ();

		// pp.outputAllResourcesAndSourcesForEachTarget();

		// pp.compareSameTargetsBetweenTwoXCODEPROJFiles("develop(beforeChristine).xcodeproj","master-to-develop.xcodeproj");
	}

}
