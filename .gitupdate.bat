@echo off
git remote add origin https://github.com/emery303/photoselecta
git add -A
git commit -m "automatic update"
git push -f origin master
git pull https://github.com/emery303/photoselecta
