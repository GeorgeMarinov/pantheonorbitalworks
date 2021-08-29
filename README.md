# Starsector Mod Template

This is a basic Github template to create a working Starsector mod.
Under the hood it uses a Gradle automation tool to fetch dependencies and compile source code.

## Usage

1. Have Java SDK installed, anything 7 or above will work
1. Pick a mod name (can have spaces, e.g. "My Starsector Mod") and mod identifier (cannot have spaces, only A-z, "mymod", try to make it unique across mod-verse)
1. Click "Use this template", use your mod identifier as "Repository name"
1. Run `./gradlew build` or `gradlew.bat build` (depending on your operating system) - you will get "empty" jar (empty because there is no code yet)

## Adding scripts

To start creating your mod:

1. Create a folder in `src/main/java` with the mod identifier name (e.g. `src/main/java/mymod`).
1. (Optional) Add your mod plugin, recommended would be `src/main/java/mymod/MymodPlugin.java` - this file "lives" in `package mymod;` and initializes your mod.
1. Write remaining code.
1. Build, package, release.

## Building

Already covered in usage, just run `./gradlew build` or `gradlew.bat build`.

## Packaging

1. Create `assets/mod_info.json` with correct structure.
1. (optional) Create additional content in `assets/`:
   1. `data` and `graphics` folders (if any) with their content,
   1. any other files you want to bundle.
1. Run `./gradlew release` (`mod_info.json`'s version will be used)


## Releasing (Automatic)

Tag a commit and push it - tag name will become a release name, and commit message will become a release message.

```sh
git commit -m "My first release" # you can just `git commit` and type a body of release as well
git push origin master           # Send the commit to your repo (no release yet)
git tag 1.0.0                    # No release yet, it's only local now
git push --tags origin master    # Release is happening now
```

## Releasing (Manual)

1. Click "Create a release" (either from main page of your repository or from "Releases" tab).
1. Name a tag, add title, and add description (optional, usually a list of changes made in this version).
1. Add zip file you created in Packaging section and click "Publish release".
