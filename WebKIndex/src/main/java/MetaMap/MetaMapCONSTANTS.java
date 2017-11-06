package MetaMap;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Class for MetaMap API Constants
 *
 * Created by Thodoris Tsompanidis on 18/12/2015.
 */
public final class MetaMapCONSTANTS {
	
	public static final String USERNAME="";
	public static final String PASSWORD="";
	public static final String EMAIL="";


	public static final String METAMAP_LOCAL_IP = "160.40.X.Y";
	public static final String SemRep_LoCAL_PORT = "8087";
	public static final String SemRep_LoCAL_PATH = "semrep.php";
	public static final String SemRep_LoCAL_TEXT_PARAMETER = "text";

	public static final ArrayList<String> diseaseList = new ArrayList<>(Arrays.asList("DiseaseOrSyndrome",
			"AcquiredAbnormality","Bacterium","SignOrSymptom","AnatomicalAbnormality",
			"CellOrMolecularDysfunction","InjuryOrPoisoning","MentalOrBehavioralDysfunction",
			"NeoplasticProcess","PathologicFunction"));
	public static final ArrayList<String> treatmentList = new ArrayList<>(Arrays.asList("TherapeuticOrPreventiveProcedure",
			"MedicalDevice","Antibiotic","ClinicalDrug","PharmacologicSubstance"));
	public static final ArrayList<String> testList = new ArrayList<>(Arrays.asList("DiagnosticProcedure","LaboratoryProcedure"));

}
