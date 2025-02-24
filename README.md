# Introduction

[![CI](https://github.com/WhosNickDoglio/multi-file-kotlin-converter-plugin/actions/workflows/ci.yml/badge.svg)](https://github.com/WhosNickDoglio/multi-file-kotlin-converter-plugin/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

The __Multiple File Kotlin Converter__ IntelliJ IDEA plugin runs the native *Convert Java File To Kotlin File* action against a list of files,
in sequence allowing bulk conversion. It includes several options for selecting files, including:
- New-line separated list of files
- `regex` matching (Currently limited to filename)
- Line-count based filtering

The `regex` and line-count based options include a verification and selection step.

All conversions also include an optional step to perform an in-place renaming of the files from `.java` to `.kt` so that `git`/`Version Control` history is maintained.


# How to install?

The plugin is published under the *JetBrains Plugins Repository* (see [here](https://plugins.jetbrains.com/plugin/12183-multiple-file-kotlin-converter/))
and can be installed following these simple steps:

1. Open __Settings__ menu (`Ctrl Alt S`).
2. Access __Plugins__ section.
3. Click __Browse repositories...__ button.
4. Search for __Multiple File Kotlin Converter__ and click __Install__ button.


# How does it work?

This plugin adds a new *Convert Multiple Files To Kotlin...* menu action right after *Convert Java File To Kotlin File*
native menu (under *Code* menu).

When running this new action menu, the following steps are applied to each selected Java file:
- Rename Java file with Kotlin extension
- Commit renaming step to VCS with standard or custom commit message
- Rename file back to Java extension

The plugin invokes the native *Convert Java File To Kotlin File* action on all selected Java files.

# License

    Copyright 2025 Nicholas Doglio
    Copyright 2019 Pandora Media, LLC

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
