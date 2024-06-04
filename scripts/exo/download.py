import requests
import threading
import zipfile
import os


def download(url : str, destination : str) -> None:
    zip_file = f"{destination}.zip"
    response = requests.get(url)
    if response.status_code == 200:
        with open(zip_file, "wb") as file:
            file.write(response.content)
        print(f"Downloaded {url} to {zip_file} successfully")
        if not os.path.exists(f"./{destination}"):
            os.makedirs(destination)
        with zipfile.ZipFile(zip_file, "r") as zip:
            zip.extractall(f"./{destination}")
        print(f"Extracted file from {zip_file}")
        os.remove(zip_file)
        print("Removed zip file")
    else:
        print(f"Failed to download {url}")


def main():
    files = [
            "citcrc",   #autos Chambly-Richelieu-Carignan
            "cithsl",   #autos Haut-Saint-Laurent
            "citla",    #autos Laurentides
            "citpi",    #autos La Presqu'île
            "citlr",    #autos Le Richelain
            "citrous",  #autos Roussillon
            "citsv",    #autos Sorel-Varennes
            "citso",    #autos Sud-ouest
            "citvr",    #autos Vallée du Richelieu
            "mrclasso", #autos L'Assomption
            "mrclm",    #autos Terrebonne-Mascouche
            "trains",
            "omitsju",  #autos Sainte-Julie
            "lrrs"      #autos Le Richelain et Roussillon
    ]
    jobs = []

    for file in files:
        thread = threading.Thread(target=download, args=(
            "https://exo.quebec/xdata/" + file + "/google_transit.zip", file
        ))
        jobs.append(thread)
        thread.start()

    for job in jobs:
        job.join()


if __name__ == "__main__":
    main()