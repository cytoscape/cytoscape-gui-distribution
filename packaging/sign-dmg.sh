#!/bin/bash

SIGNING_IDENTITY="${1}"

if [ -z "${SIGNING_IDENTITY}" ]
then
    echo "Usage: ${0} signing-identity"
    echo
    echo "... where signing-identity is the name of a private key/certificate pair in your Keychain (e.g. 'Developer ID Application')."
    exit 1
fi

ORIGINAL_DMG="target/install4j/Cytoscape_*_macos.dmg"
MOUNT_DIR=target/mount
SIGN_DIR=target/tempsign
TARGET_DIR=target/install4j/signed
TARGET_DMG="${TARGET_DIR}/$(basename ${ORIGINAL_DMG})"

# Maximum size of DMG contents in KB
DMG_SIZE_LIMIT=120000

mkdir "${MOUNT_DIR}"
mkdir "${SIGN_DIR}"
mkdir "${TARGET_DIR}"

echo Mounting original DMG...

DEVICE=$(hdiutil attach -noverify -noautoopen -mountroot `pwd`/target/mount ${ORIGINAL_DMG} | egrep '^/dev/' | sed 1q | awk '{print $1}') || exit 1

VOLUME_DIR="$(basename target/mount/Cytoscape*)"

echo Extracting installer...

cp -r target/mount/Cytoscape*/Cytoscape\ Installer.app "${SIGN_DIR}"
hdiutil detach "${DEVICE}"

echo Signing installer...

rm target/tempsign/Cytoscape\ Installer.app/Contents/vmoptions.txt

codesign -f -s "${SIGNING_IDENTITY}" "${SIGN_DIR}"/Cytoscape\ Installer.app
codesign -vvv "${SIGN_DIR}"/Cytoscape\ Installer.app || exit 1

echo Creating new DMG in \"${TARGET_DIR}\"...

rm "${TARGET_DMG}" 2>&1 >/dev/null
rm target/temp.dmg 2>&1 >/dev/null

hdiutil create -srcfolder "${SIGN_DIR}" -volname "${VOLUME_DIR}" -fs HFS+ -fsargs "-c c=64,a=16,e=16" -format UDRW -size ${DMG_SIZE_LIMIT}k target/temp.dmg || exit 1
hdiutil convert target/temp.dmg -format UDZO -imagekey zlib-level=9 -o "${TARGET_DMG}" || exit 1

echo Done!
