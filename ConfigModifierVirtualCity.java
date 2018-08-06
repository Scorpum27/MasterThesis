package ch.ethz.matsim.students.samark;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;

public class ConfigModifierVirtualCity {

	public static Config modifyConfig(Config config) {
		
		config.getModules().get("changeMode").addParam("modes", "walk,car,pt");
		config.getModules().get("controler").addParam("lastIteration", "100");
		// System.out.println(config.getModules().get("controler").getParams().get("lastIteration").toString());
		config.getModules().get("controler").addParam("outputDirectory", "myInput/Simulation_Output_VirtualCity");
		config.getModules().remove("facilities");
		config.getModules().remove("households");
		config.getModules().get("network").addParam("inputNetworkFile", "myInput/Networks/Network_50x50_20PercentLean.xml");
		config.getModules().get("plans").addParam("inputPersonAttributesFile", "null"); // may have to add attributes in population creation !!
		config.getModules().get("plans").addParam("inputPlansFile", "myInput/Populations/plans100.xml");
		// may have to add whole new ParameterSet here by creating it...  old attempt was: config.getModules().get("strategy").getParameterSets().get("strategysettings") //
		config.getModules().get("transit").addParam("transitScheduleFile","myInput/PT/scheduleOne.xml");
		config.getModules().get("transit").addParam("vehiclesFile","myInput/PT/vehicles.xml");
		config.getModules().get("changeMode").addParam("modes", "walk,car,pt");
		config.getModules().get("changeMode").addParam("modes", "walk,car,pt");
		config.controler().setOverwriteFileSetting(OverwriteFileSetting.overwriteExistingFiles);
		
		ConfigWriter configWriter = new ConfigWriter(config);
		configWriter.write("myInput/zurich_config_BlankModified.xml");
		
		return config;
	}
	
public static Config modifyConfigFromFile(String configFile) {
		
		Config modConfig = ConfigUtils.loadConfig(configFile);
	
		modConfig.getModules().get("changeMode").addParam("modes", "walk,car,pt");
		modConfig.getModules().get("controler").addParam("lastIteration", "3");
		// System.out.println(config.getModules().get("controler").getParams().get("lastIteration").toString());
		modConfig.getModules().get("controler").addParam("outputDirectory", "Simulation_Output");
		modConfig.getModules().remove("facilities");
		modConfig.getModules().remove("households");
		modConfig.getModules().get("network").addParam("inputNetworkFile", "Networks/Network_50x50_20PercentLean.xml");
		modConfig.getModules().get("plans").addParam("inputPersonAttributesFile", "null"); // may have to add attributes in population creation !!
		modConfig.getModules().get("plans").addParam("inputPlansFile", "Populations/plans100.xml");
		// may have to add whole new ParameterSet here by creating it...  old attempt was: config.getModules().get("strategy").getParameterSets().get("strategysettings") //
		modConfig.getModules().get("transit").addParam("transitScheduleFile","PT/scheduleOne.xml");
		modConfig.getModules().get("transit").addParam("vehiclesFile","PT/vehicles.xml");
		modConfig.getModules().get("changeMode").addParam("modes", "walk,car,pt");
		modConfig.getModules().get("changeMode").addParam("modes", "walk,car,pt");
		modConfig.controler().setOverwriteFileSetting(OverwriteFileSetting.overwriteExistingFiles);
		
		ConfigWriter configWriter = new ConfigWriter(modConfig);
		configWriter.write("myInput/zurich_config_modified.xml");
		
		return modConfig;
	}
	
}
