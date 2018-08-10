package ch.ethz.matsim.students.samark;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;

public class Metro_ConfigModifier {

	public static Config modifyFromFile(String configFile) {

		Config modConfig = ConfigUtils.loadConfig(configFile);

		modConfig.controler().setOverwriteFileSetting(OverwriteFileSetting.overwriteExistingFiles);
		modConfig.getModules().get("controler").addParam("outputDirectory", "Metro/Simulation_Output");
		modConfig.getModules().get("network").addParam("inputNetworkFile", "Metro/Input/Generated_Networks/MergedNetwork.xml");
		modConfig.getModules().get("transit").addParam("transitScheduleFile","Metro/Input/Generated_PT_Files/MergedSchedule.xml");
		modConfig.getModules().get("transit").addParam("vehiclesFile","Metro/Input/Generated_PT_Files/MergedVehicles.xml");

		
		ConfigWriter configWriter = new ConfigWriter(modConfig);
		configWriter.write("Metro/Input/Generated_Config/zurich_config_metro.xml");
		
		return modConfig;
	}
	
}
