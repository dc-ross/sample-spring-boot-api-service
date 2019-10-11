import { Command } from "@oclif/command";
import { existsSync, lstatSync, readdirSync } from "fs";
import * as logSymbols from "log-symbols";
import { readConfiguration } from "../config";
import { execSshCommandWithDefaultEnv, execSshCommandWithDefaultEnvCwd, zoweSync } from "../zowe";

export default class ZosBuild extends Command {
    static description = "build z/OS source on z/OS UNIX";

    async run() {
        const [userConfig, projectConfig] = readConfiguration(this);
        const zosDir = `${userConfig.zosTargetDir}/${projectConfig.zosSourcesDir}`;
        execSshCommandWithDefaultEnvCwd(`mkdir -p ${zosDir}`);
        uploadDir(projectConfig.zosSourcesDir, zosDir, this);
        const env: { [name: string]: string } = {};
        if (userConfig.javaHome) {
            env.JAVA_HOME = userConfig.javaHome;
        }
        this.log(`Building z/OS native code`);
        execSshCommandWithDefaultEnv(projectConfig.buildCommand, zosDir, env);
        for (const [zosFile, targetFile] of Object.entries(projectConfig.buildFiles)) {
            this.log(`Downloading ${zosDir}/${zosFile} to ${targetFile}`);
            zoweSync(`files download uss-file ${zosDir}/${zosFile} --binary -f ${targetFile}`);
        }
        this.log(logSymbols.success, "z/OS build completed");
        this.log(logSymbols.info, "Use 'zowe-api-dev deploy' to deploy your application to z/OS");
    }
}

function uploadDir(dir: string, zosDir: string, command: Command) {
    if (existsSync(dir) && lstatSync(dir).isDirectory()) {
        const files = readdirSync(dir);
        files.forEach(file => {
            const targetFile = `${zosDir}/${file}`;
            command.log(`Uploading ${file} to ${targetFile}`);
            zoweSync(`files upload ftu "${dir}/${file}" "${targetFile}"`);
        });
    } else {
        command.error(`Directory '${dir}' does not exist`);
    }
}