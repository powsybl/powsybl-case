/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.caseserver;

import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.powsybl.caseserver.CaseConstants.*;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
@Service
public class CaseService {

    private FileSystem fileSystem = FileSystems.getDefault();

    Map<String, String> getCaseList() {
        checkStorageInitialization();

        Map<String, String> cases;
        try (Stream<Path> walk = Files.walk(getStorageRootDir())) {
            cases = walk.filter(Files::isRegularFile)
                    .collect(Collectors.toMap(Object::toString, x -> x.getFileName().toString()));
        } catch (IOException e) {
            return null;
        }
        return cases;
    }

    Path getCase(String caseName) {
        checkStorageInitialization();
        Path file = getStorageRootDir().resolve(caseName);
        if (Files.exists(file) && Files.isRegularFile(file)) {
            return file;
        }
        return null;
    }

    boolean exists(String caseName) {
        Path file = getStorageRootDir().resolve(caseName);
        return Files.exists(file) && Files.isRegularFile(file);
    }

    void importCase(MultipartFile mpf) {
        checkStorageInitialization();

        Path file = getStorageRootDir().resolve(mpf.getOriginalFilename());
        if (Files.exists(file) && Files.isRegularFile(file)) {
            throw new CaseException(FILE_ALREADY_EXISTS);
        }
        try {
            mpf.transferTo(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    Network downloadNetwork(String caseName) {
        checkStorageInitialization();
        Path caseFile = getStorageRootDir().resolve(caseName);
        Network network = Importers.loadNetwork(caseFile);
        if (network == null) {
            throw new CaseException(FILE_NOT_IMPORTABLE);
        }
        return network;
    }

    void deleteCase(String caseName) {
        checkStorageInitialization();

        Path file = getStorageRootDir().resolve(caseName);
        if (Files.exists(file) && !Files.isRegularFile(file)) {
            throw new CaseException(FILE_DOESNT_EXIST);
        }
        try {
            Files.delete(file);
        } catch (NoSuchFileException e) {
            throw new CaseException(FILE_DOESNT_EXIST);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    void deleteAllCases() {
        checkStorageInitialization();

        Path caseFolder = getStorageRootDir();
        if (!Files.isDirectory(caseFolder)) {
            throw new CaseException(DIRECTORY_DOESNT_EXIST);
        }
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(caseFolder)) {
            paths.forEach(file -> {
                try {
                    Files.delete(file);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private boolean isStorageCreated() {
        Path storageRootDir = getStorageRootDir();
        return Files.exists(storageRootDir) && Files.isDirectory(storageRootDir);
    }

    private Path getStorageRootDir() {
        return fileSystem.getPath(System.getProperty(USERHOME) + CASE_FOLDER);
    }

    private void checkStorageInitialization() {
        if (!isStorageCreated()) {
            throw new CaseException(STORAGE_DIR_NOT_CREATED);
        }
    }

    void setFileSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }
}
