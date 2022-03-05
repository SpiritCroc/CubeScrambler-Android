#!/bin/bash

mydir="$(dirname "$(realpath "$0")")"

base_out="$mydir/../app/src/main/res"

export_files() {
    newfile="$(basename "$file" .svg).png"
    mkdir -p $base_folder-mdpi
    mkdir -p $base_folder-hdpi
    mkdir -p $base_folder-xhdpi
    mkdir -p $base_folder-xxhdpi
    mkdir -p $base_folder-xxxhdpi
    inkscape "$file" --export-filename="$base_folder-mdpi/$newfile" -C --export-dpi=$dpi
    inkscape "$file" --export-filename="$base_folder-hdpi/$newfile" -C --export-dpi=$(($dpi*3/2))
    inkscape "$file" --export-filename="$base_folder-xhdpi/$newfile" -C --export-dpi=$(($dpi*2))
    inkscape "$file" --export-filename="$base_folder-xxhdpi/$newfile" -C --export-dpi=$(($dpi*3))
    inkscape "$file" --export-filename="$base_folder-xxxhdpi/$newfile" -C --export-dpi=$(($dpi*4))
}

base_folder="$base_out/mipmap"
dpi=24 # 96/4

file="$mydir/ic_launcher.svg"
export_files

dpi=48 # 96/2

file="$mydir/ic_launcher_foreground.svg"
export_files
