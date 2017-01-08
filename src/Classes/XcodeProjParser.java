package Classes;

import java.io.IOException;
import java.util.*;

public class XcodeProjParser {

	//These are the key words in pbxproj file

    //This contains your target list & some other info (regions, dev team...)
    private final static String BeginPBXProjectSection = "Begin PBXProject section";
    private final static String SEARCH_TARGET_BEGIN_TOKEN = "targets = (";
    private final static String SEARCH_END_TOKEN = ");";

    //This contains Sources & Resources ID for your targets
	private final static String BeginPBXNativeTargetSection = "Begin PBXNativeTarget section";
	private final static String EndPBXNativeTargetSection = "End PBXNativeTarget section";

    private final static String SEARCH_TARGET_INFO_BEGIN = "buildPhases = (";

    //All resources beginning
	private final static String BeginPBXResourcesBuildPhaseSection = "Begin PBXResourcesBuildPhase section";

    //All sources beginning
	private final static String BeginPBXSourcesBuildPhaseSection = "Begin PBXSourcesBuildPhase section";

	private final static String TARGETINFO_FILENAME = "TargetsInfo";


	private final static String RESOURCES = "Resources";
	private final static String SOURCES = "Sources";

	private String referenceTargetName;

    // storing target objects
	private Map<String, TargetObject> targetHm;
    // for comparing between targets
	private Map<String, Object> compareHm;

    // for storing referenceTarget's Recources or Sources
	private ArrayList<String> al;
	private ArrayList<String> backwardsAl;

	//name of you xcodeproj file
	private String xcodeprojName;

	public XcodeProjParser(String xcodeproj) throws IOException {
		this(xcodeproj, "TODO");
	}

	public XcodeProjParser(String xcodeproj, String referenceTarget)
			throws IOException {

		if (xcodeproj.contains("xcodeproj")) {
			xcodeprojName = xcodeproj + "/project.pbxproj";
			referenceTargetName = xcodeproj.trim().split("[.]")[0];
		} else if (xcodeproj.contains("pbxproj")) {
			xcodeprojName = xcodeproj;
		}
		referenceTargetName = referenceTarget;
		// use it to compare with other targets
		targetHm = new HashMap<String, TargetObject>();
	}

	/**
	 * For ONLY ONE Xcodeproj file. Use target Alarm as a reference, and compare
	 * the rest targets to see what files are missing
	 */
	public void compareBetweenTargetsInOneXCODEPROJ() throws IOException {

		BufferReaderHelper brHelper = new BufferReaderHelper(xcodeprojName);

//		generateAllTargetObjects(brHelper);
//		findAllRespectiveResourcesAndSources(brHelper);

		outputAllTargetInfo();

//		findAllMissingFilesInDifferentTargets(RESOURCES, brHelper);
//		findAllMissingFilesInDifferentTargets(SOURCES, brHelper);

		outputDone();
		brHelper.closeReaderBuffers();
	}

	public void outputAllResourcesAndSourcesForEachTarget() throws IOException {

	    //read buffer
		BufferReaderHelper brHelper = new BufferReaderHelper(xcodeprojName);

		generateAllTargetObjects(brHelper);
		findAllRespectiveResourcesAndSources(brHelper);

		writeResourcesAndSourcesFromTargets(RESOURCES, brHelper);// output the whole list
		writeResourcesAndSourcesFromTargets(SOURCES, brHelper);

		outputDone();
		brHelper.closeReaderBuffers();
	}

	/**
	 * For TWO Xcodeproj files Compare same targets: (Alarm target in xcodeproj1
	 * and Alarm target in xcodeproj2 to see what is missing)
	 * 
	 */
	public void compareSameTargetsBetweenTwoXCODEPROJFiles(String xp1,
			String xp2) throws IOException {

		if (xp1.contains("xcodeproj")) {
			xp1 = xp1 + "/project.pbxproj";
		} else
			return;
		if (xp2.contains("xcodeproj")) {
			xp2 = xp2 + "/project.pbxproj";
		} else
			return;

		BufferReaderHelper brHelper1 = new BufferReaderHelper(xp1);
		generateAllTargetObjects(brHelper1);
		findAllRespectiveResourcesAndSources(brHelper1);
		Map<String, TargetObject> hmTarget1 = new HashMap<String, TargetObject>(
				targetHm);

		targetHm = new HashMap<String, TargetObject>();

		BufferReaderHelper brHelper2 = new BufferReaderHelper(xp2);
		generateAllTargetObjects(brHelper2);
		findAllRespectiveResourcesAndSources(brHelper2);
		Map<String, TargetObject> hmTarget2 = new HashMap<String, TargetObject>(
				targetHm);

		String xp1Name = xp1.trim().split("[.]")[0];
		String xp2Name = xp2.trim().split("[.]")[0];

		findDifferenceBetweenTwoProject(RESOURCES, brHelper1, brHelper2,
				hmTarget1, hmTarget2, xp1Name, xp2Name);
		findDifferenceBetweenTwoProject(SOURCES, brHelper1, brHelper2,
				hmTarget1, hmTarget2, xp1Name, xp2Name);

		outputDone();
		brHelper1.closeReaderBuffers();
		brHelper2.closeReaderBuffers();
	}

	/*
	    Private helpers
	 */
    private void generateAllTargetObjects(BufferReaderHelper brHelper)
            throws IOException {
        brHelper.findLine(BeginPBXProjectSection, true);
        brHelper.findLine(SEARCH_TARGET_BEGIN_TOKEN, true);

        while (!brHelper.getLine(SEARCH_END_TOKEN)) {
            TargetObject obj = getTargetObject(brHelper.getLastLine());
            targetHm.put(obj.targetName, obj);
        }
    }

    private ArrayList<String> splitTargetIdentifierLine(String line) {
		String[] tmp = line.trim().split("\\*");
		ArrayList<String> arr = new ArrayList<>();
		for(String cur : tmp) {
			cur = cur.replaceAll("[/*,]", "").trim();
			arr.add(cur);
		}
		return arr;
	}
    private TargetObject getTargetObject(String line) {
        ArrayList<String> arr = splitTargetIdentifierLine(line);
        return new TargetObject(arr.get(1), arr.get(0));
    }

	private void findDifferenceBetweenTwoProject(String code,
			BufferReaderHelper brHelper1, BufferReaderHelper brHelper2,
			Map<String, TargetObject> hmTarget1,
			Map<String, TargetObject> hmTarget2, String xp1, String xp2)
			throws IOException {

		for (String key : hmTarget1.keySet()) { // compare same target

			TargetObject tv = hmTarget1.get(key);
			BufferWriterHelper bwHelper = new BufferWriterHelper(code
					+ "_difference_" + tv.targetName);

			bwHelper.writeAndLog("<" + code + " difference between "
					+ tv.targetName + " targets>");
			bwHelper.writeAndLog();

			// 1.find the line for the respective list, xcodeproj 1
			if (code.equals(RESOURCES)) {
				findList(tv.resourcesCode, true, brHelper1);
			} else {
				findList(tv.sourcesCode, false, brHelper1);
			}
			storeListIntoHashMap(brHelper1, bwHelper, tv.targetName);
			HashMap<String, Object> compareHm1 = new HashMap<String, Object>(
					compareHm);

			// 2.find the line for the respective list, xcodeproj 2
			if (code.equals(RESOURCES)) {
				findList(tv.resourcesCode, true, brHelper2);
			} else {
				findList(tv.sourcesCode, false, brHelper2);
			}
			storeListIntoHashMap(brHelper2, bwHelper, tv.targetName);
			HashMap<String, Object> compareHm2 = new HashMap<String, Object>(
					compareHm);

			// 3.compare bilateral
			ArrayList<String> hm2Missing = getMissingFiles(compareHm1,
					compareHm2);

			ArrayList<String> hm1Missing = getMissingFiles(compareHm2,
					compareHm1);

			outputDifferenceBetweenTwoTargets(hm1Missing, bwHelper, xp1, xp2,
					key, code);
			outputDifferenceBetweenTwoTargets(hm2Missing, bwHelper, xp2, xp1,
					key, code);

			bwHelper.closeWriterBuffers();
		}
	}

	private void findAllRespectiveResourcesAndSources(
			BufferReaderHelper brHelper) throws IOException {

		brHelper.findLine(BeginPBXNativeTargetSection, false);//false means search from top again
		while (!brHelper.getLastLine().contains(EndPBXNativeTargetSection)) {
			//find beginning
			if (brHelper.readOneLine().contains("= {")) {
				TargetObject targetObject = getTargetObject(brHelper.getLastLine());

				//build phases
				brHelper.findLine(SEARCH_TARGET_INFO_BEGIN, true);
				while(!brHelper.getLastLine().contains(");")) {
					String lastLine = brHelper.readOneLine();
					if (brHelper.getLastLine().contains(RESOURCES)) {
						ArrayList<String> arr = splitTargetIdentifierLine(lastLine);
						targetObject.resourcesCode = arr.get(0);// assign resource code
					} else if (brHelper.getLastLine().contains(SOURCES)) {
						ArrayList<String> arr = splitTargetIdentifierLine(lastLine);
						targetObject.sourcesCode = arr.get(0);// assign resource code
					}
				}
				targetHm.put(targetObject.targetName, targetObject);
			}
		}

		for (TargetObject tt : targetHm.values()) {
			System.out.println(tt);
		}
	}

	// find the Resources or Sources for a specific target. Use it combining
	// with targetObj
	private void findList(String RorSCode, boolean isResource,
			BufferReaderHelper brHelper) throws IOException {
		if (isResource)
			brHelper.findLine(BeginPBXResourcesBuildPhaseSection, false);
		else
			brHelper.findLine(BeginPBXSourcesBuildPhaseSection, false);
		brHelper.findLine(RorSCode, true);
		brHelper.findLine("files = (", true);
	}

	/**
	 * For reference target
	 */
	private void storeListIntoArrayList(BufferReaderHelper brHelper)
			throws IOException {
		al = new ArrayList<String>();
		String lastLine = brHelper.readOneLine();

		while (!lastLine.contains(SEARCH_END_TOKEN)) {
			String fileName = getFileNameFromLine(lastLine);

			// special files we need to ignore
			if (!stringContainsIgnoredFiles(fileName)) {
				al.add(fileName);
			}
			lastLine = brHelper.readOneLine();
		}
	}

	private void storeListIntoHashMap(BufferReaderHelper brHelper,
			BufferWriterHelper bwHelper, String targetName) throws IOException {
		compareHm = new HashMap<String, Object>();
		Object obj = new Object();
		String lastLine = brHelper.readOneLine();
		while (!lastLine.contains(SEARCH_END_TOKEN)) {
			String fileName = getFileNameFromLine(lastLine);

			// special files we need to ignore
			if (!stringContainsIgnoredFiles(fileName)) {
				if (compareHm.containsKey(fileName)) {
					bwHelper.writeAndLog(true);
					bwHelper.writeAndLog(
							"********************************************************************************************************************",
							true);
					bwHelper.writeAndLog("******* Duplicated file exists in "
							+ targetName + " target: " + fileName
							+ " --- (multiLine with same name) *******", true);
					bwHelper.writeAndLog(
							"********************************************************************************************************************",
							true);
					bwHelper.writeAndLog();
				} else
					compareHm.put(fileName, obj);
			}

			lastLine = brHelper.readOneLine();
		}
	}

	private void writeResourcesAndSourcesFromTargets(String code,
			BufferReaderHelper brHelper) throws IOException {

		for (TargetObject tv : targetHm.values()) {

			BufferWriterHelper bwHelper = new BufferWriterHelper(code + "_"
					+ tv.targetName);
			if ((code == SOURCES && tv.sourcesCode == null) || (code == RESOURCES && tv.resourcesCode == null)) {
				bwHelper.writeAndLog("****** <" + tv.targetName + "> doesn't have "+ code + " code. ********");
				bwHelper.closeWriterBuffers();
				continue;
			}

			bwHelper.writeAndLog("****** <" + code + " for " + tv.targetName
					+ "> ********");
			bwHelper.writeAndLog();

			if (code.equals(RESOURCES)) {
				findList(tv.resourcesCode, true, brHelper);
			} else {
				findList(tv.sourcesCode, false, brHelper);
			}

			int count = 0;
			HashSet<String> hs = new HashSet<>();
			ArrayList<String> duplicated = new ArrayList<>();
			while (!brHelper.readOneLine().contains(SEARCH_END_TOKEN)) {
				ArrayList<String> arr = splitTargetIdentifierLine(brHelper.getLastLine());
				String name = arr.get(1);

				//skip some type of files
				if (stringContainsIgnoredFiles(name)) {
					continue;
				}

				if (hs.contains(name)) {
					//output
					duplicated.add(name);
				} else {
					hs.add(name);
				}
				bwHelper.writeAndLog(name);
				count++;
			}
			bwHelper.writeAndLog();
			bwHelper.writeAndLog("****** <" + code + " for " + tv.targetName
					+ ">, total " + count + " files********");
			bwHelper.writeAndLog();

			for (String dup : duplicated) {
				bwHelper.writeAndLog("---Duplicated file(multiLine with same name): " + dup, true);
			}

			bwHelper.closeWriterBuffers();
		}
	}

	private void findAllMissingFilesInDifferentTargets(String code,
			BufferReaderHelper brHelper) throws IOException {

		BufferWriterHelper bwHelper = new BufferWriterHelper(code);

		if (targetHm.size() <= 1) {
			outputNotEnoughTargetToCompare(bwHelper, code);
			return;
		}

		outputBeginOrEndTargetComparing(bwHelper, code, true);

		// ---- For the reference target
		TargetObject ta = targetHm.get(referenceTargetName);
		if (ta == null) {
			bwHelper.writeAndLog("**** " + referenceTargetName
					+ " target not exists ****");
			bwHelper.writeAndLog();
			return;
		}

		if (code.equals(RESOURCES)) {
			findList(ta.resourcesCode, true, brHelper);
		} else {
			findList(ta.sourcesCode, false, brHelper);
		}

		storeListIntoArrayList(brHelper);
		// ----

		for (String key : targetHm.keySet()) {
			if (key.equals(referenceTargetName))// we don't compare the same
												// target
				continue;
			TargetObject tv = targetHm.get(key);
			if (code.equals(RESOURCES)) {
				findList(tv.resourcesCode, true, brHelper);
			} else {
				findList(tv.sourcesCode, false, brHelper);
			}
			storeListIntoHashMap(brHelper, bwHelper, tv.targetName);
			ArrayList<String> resultForMissingFiles = getMissingFiles();
			// write and log compared results
			outputComparingResults(key, code, resultForMissingFiles,
					backwardsAl, bwHelper, tv.targetName);
		}
		outputBeginOrEndTargetComparing(bwHelper, code, false);
		bwHelper.closeWriterBuffers();
	}

	/**
	 * for two project files
	 * 
	 * @param hm1
	 * @param hm2
	 * @return
	 */
	private ArrayList<String> getMissingFiles(HashMap<String, Object> hm1,
			HashMap<String, Object> hm2) {

		ArrayList<String> r = new ArrayList<String>();

		for (String tmp : hm1.keySet()) {

			if (hm2.get(tmp) == null) {
				// just skip the localizable and txt files
				if (stringContainsIgnoredFiles(tmp))
					continue;
				r.add(tmp);
			} else {
				hm2.remove(tmp);
			}
		}
		// r means hm2 is missing the files within
		return r;
	}

	/**
	 * compare bidirectionally
	 * 
	 */
	private ArrayList<String> getMissingFiles() {

		ArrayList<String> r = new ArrayList<String>();
		HashMap<String, Object> copyHm = new HashMap<String, Object>(compareHm);
		boolean missingReverse = false;
		// reference target size less than comparing target
		if (al.size() < copyHm.size()) {
			missingReverse = true;
		}

		for (String tmp : al) {
			if (copyHm.get(tmp) == null) {
				if (stringContainsIgnoredFiles(tmp))
					continue;
				r.add(tmp);
			} else {
				copyHm.remove(tmp);
			}
		}

		if (missingReverse) {
			backwardsAl = new ArrayList<String>();
			for (String tmp : copyHm.keySet()) {
				backwardsAl.add(tmp);
			}
		}

		return r;
	}

	/**
	 * String Helpers
	 */

	private boolean stringContainsIgnoredFiles(String fileName) {
		return fileName.contains(".strings") || fileName.contains(".txt")
				|| fileName.contains(".app");
	}

	/**
	 * get the file name from the whole string: eg 123.png, viewController.m
	 */
	private String getFileNameFromLine(String wholeLine) {
		String[] tmp = wholeLine.trim().split("/\\*");
		tmp[1] = tmp[1].replaceAll("[/*,]", "").trim()
				.split("(in Sources)|(in Resources)")[0].trim();
		return tmp[1];
	}

	/**
	 * OUTPUT Helpers
	 */
	private void outputAllTargetInfo() throws IOException {

		BufferWriterHelper bwHelper = new BufferWriterHelper(
				TARGETINFO_FILENAME);
		bwHelper.writeAndLog("--------- " + targetHm.size()
				+ " Targets info ---------");
		bwHelper.writeAndLog();
		for (TargetObject to : targetHm.values()) {
			bwHelper.writeAndLog(to.toString());
		}
		bwHelper.writeAndLog("--------- End " + targetHm.size()
				+ " Targets info ---------");
		bwHelper.writeAndLog();
		bwHelper.closeWriterBuffers();
	}

	// private void outputComparingResults(String key, String code,
	// ArrayList<String> resultForMissingFiles, BufferWriterHelper bwHelper) {
	//
	// outputComparingResults(key, code, resultForMissingFiles, null, bwHelper);
	// }

	private void outputComparingResults(String key, String code,
			ArrayList<String> resultForMissingFiles,
			ArrayList<String> backwardsMissingFiles,
			BufferWriterHelper bwHelper, String comparingTargetName) {
		bwHelper.writeAndLog("Comparing <" + referenceTargetName + "> and <"
				+ key + "> :");
		String excludeStr;
		if (code.equals(RESOURCES))
			excludeStr = ".txt, .string";
		else
			excludeStr = ".app";
		bwHelper.writeAndLog(referenceTargetName + " " + code + " size: "
				+ this.al.size() + "  ------- (excluding " + excludeStr + ")");
		bwHelper.writeAndLog(key + " " + code + " size: "
				+ compareHm.values().size());
		if (resultForMissingFiles.size() == 0 && backwardsAl == null) {
			outputPassed(bwHelper);
		} else {

			if (resultForMissingFiles.size() > 0) {
				outputMissingFiles(comparingTargetName, resultForMissingFiles,
						referenceTargetName, bwHelper, code);
			}

			bwHelper.writeAndLog();

			if (backwardsAl != null && backwardsAl.size() > 0) {
				outputMissingFiles(referenceTargetName, backwardsAl,
						comparingTargetName, bwHelper, code);
				backwardsAl = null;
			}
		}
		bwHelper.writeAndLog();
	}

	private void outputPassed(BufferWriterHelper bwHelper) {
		bwHelper.writeAndLog("<~ Passed! NO Missing File ~>");
	}

	private void outputMissingFiles(String targetName,
			ArrayList<String> targetAL, String fromTargetName,
			BufferWriterHelper bwHelper, String code) {
		bwHelper.writeAndLog(true);
		bwHelper.writeAndLog(true);
		bwHelper.writeAndLog(
				"*********************************************************************************************",
				true);
		bwHelper.writeAndLog("**** <" + targetName + "> target is missing "
				+ targetAL.size() + " files from <" + fromTargetName
				+ "> target BELOW --- (" + code + ") ****", true);
		bwHelper.writeAndLog(true);
		for (String s : targetAL) {
			bwHelper.writeAndLog(s, true);
		}
		bwHelper.writeAndLog(true);
		bwHelper.writeAndLog("**** <" + targetName + "> target is missing "
				+ targetAL.size() + " files from <" + fromTargetName
				+ "> target ABOVE --- (" + code + ") ****", true);
		bwHelper.writeAndLog(
				"*********************************************************************************************",
				true);
	}

	private void outputDifferenceBetweenTwoTargets(ArrayList<String> hmMissing,
			BufferWriterHelper bwHelper, String xp1, String xp2, String key,
			String code) {
		if (hmMissing.size() == 0) {
			bwHelper.writeAndLog("--- <" + key + "> target in " + xp1
					+ ".xcodeproj is NOT MISSING any file from <" + key
					+ "> target in " + xp2 + ".xcodeproj  ---");
			bwHelper.writeAndLog();
			outputPassed(bwHelper);
			bwHelper.writeAndLog();
		} else {
			bwHelper.writeAndLog(true);
			bwHelper.writeAndLog(
					"************************************************************************************************************************",
					true);
			bwHelper.writeAndLog("**** <" + key + "> target in " + xp1
					+ ".xcodeproj is missing " + hmMissing.size()
					+ " files from <" + key + "> target in " + xp2
					+ ".xcodeproj  --- (" + code + ") ****", true);
			bwHelper.writeAndLog(true);
			bwHelper.writeAndLog(
					"********************** MISSING BELOW **********************",
					true);
			bwHelper.writeAndLog(true);
			for (String str : hmMissing) {
				bwHelper.writeAndLog(str, true);
			}
			bwHelper.writeAndLog(true);
			bwHelper.writeAndLog(
					"********************** MISSING ABOVE **********************",
					true);
			bwHelper.writeAndLog(
					"************************************************************************************************************************",
					true);
			bwHelper.writeAndLog(true);
		}
	}

	private void outputNotEnoughTargetToCompare(BufferWriterHelper bwHelper,
			String code) {
		bwHelper.writeAndLog("**** Not enough target to compare <" + code
				+ "> ****", true);
		bwHelper.writeAndLog();
	}

	private void outputBeginOrEndTargetComparing(BufferWriterHelper bwHelper,
			String code, boolean isBegin) {
		if (isBegin) {
			bwHelper.writeAndLog("------- Begin <" + code
					+ "> targets comparing -------");
		} else {
			bwHelper.writeAndLog("------- End <" + code + "> comparing -------");
		}
		bwHelper.writeAndLog();
	}

	private void outputDone() {
		System.out
				.println("\n\n\n--- Thanks! Jobs done, if no output above, it means all passed ---\n\n\n");

	}
}
