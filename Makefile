
all:
	./gradlew build

dev:
	./gradlew setupDecompWorkspace idea

clean:	
	rm -rf build .gradle .idea run out *.iml *.ipr *.iws

rel:
	docker build -t coregen .
	docker run -a stdin -a stdout -a stderr -e CF_API_TOKEN -w /coregen -it coregen make docker.rel

docker.rel:
	python3 release.py -project 310400 -name coregen -rel beta
