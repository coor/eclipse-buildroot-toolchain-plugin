package org.buildroot.cdt.toolchain;

import java.io.File;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.core.runtime.Platform;

public class BuildrootCrossEnvironmentVariableSupplier implements
		IConfigurationEnvironmentVariableSupplier {
	public IBuildEnvironmentVariable getVariable(String variableName,
			IConfiguration configuration, IEnvironmentVariableProvider provider) {
		if (PathEnvironmentVariable.isVar(variableName))
			return PathEnvironmentVariable.create(configuration);
		else
			return null;
	}

	public IBuildEnvironmentVariable[] getVariables(
			IConfiguration configuration, IEnvironmentVariableProvider provider) {
		IBuildEnvironmentVariable path = PathEnvironmentVariable
				.create(configuration);
		return path != null ? new IBuildEnvironmentVariable[] { path }
				: new IBuildEnvironmentVariable[0];
	}

	private static class PathEnvironmentVariable implements
			IBuildEnvironmentVariable {

		public static String name = "PATH";

		private File path;

		private PathEnvironmentVariable(File path) {
			this.path = path;
		}

		public static PathEnvironmentVariable create(
				IConfiguration configuration) {
			IToolChain toolchain = configuration.getToolChain().getSuperClass().getSuperClass();
			IOption option = toolchain
					.getOptionById(toolchain.getBaseId()+".option.path");
			String path = (String) option.getValue();
			File sysroot = new File(path);
			File bin = new File(sysroot, "bin");
			if (bin.isDirectory())
				sysroot = bin;
			return new PathEnvironmentVariable(sysroot);
		}

		public static boolean isVar(String name) {
			// Windows has case insensitive env var names
			return Platform.getOS().equals(Platform.OS_WIN32) ? name
					.equalsIgnoreCase(PathEnvironmentVariable.name) : name
					.equals(PathEnvironmentVariable.name);
		}

		public String getDelimiter() {
			return Platform.getOS().equals(Platform.OS_WIN32) ? ";" : ":";
		}

		public String getName() {
			return name;
		}

		public int getOperation() {
			return IBuildEnvironmentVariable.ENVVAR_PREPEND;
		}

		public String getValue() {
			return path.getAbsolutePath();
		}

	}

}
