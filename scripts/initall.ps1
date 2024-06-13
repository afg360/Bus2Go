param (
    [switch]$help = $false
)

function execute_stm {
    param(
            [string]$Arg
        )
        $path = (Get-Location).Path + "/stm"
        cd $path
        try{
            rm stm_info.db
        }
        catch{
            echo "Could not remove stm_info.db"
            echo "Either the file does not exist or is used in another application"
        }
        if ($Arg -eq "--no-download"){
            python setup_stm_db.py "no-download"
        }
        else{
            python setup_stm_db.py
        }
        $project='~/Desktop/Programming/Projects/android-apps/bus2go/android_app/app/src/main/assets/database'
        Copy-Item './stm_info.db' $project -Confirm
        cd ..
}

function execute_exo{
    param(
        [string]$Arg
    )
    $path = (Get-Location).Path + "/exo"
    cd $path
    try{
        rm exo_info.db
    }
    catch{
        echo "Could not remove exo_info.db"
        echo "Either the file does not exist or is used in another application"
    }
    if ($Arg -eq "--no-download"){
        python setup_exo_db.py "no-download"
    }
    else{
        python setup_exo_db.py
    }
    $project='~/Desktop/Programming/Projects/android-apps/bus2go/android_app/app/src/main/assets/database'
    Copy-Item './exo_info.db' $project -Confirm
    cd ..
}

if ($help) {
    echo "Script to initialise databases containing static data from transit agencies.
Usage: ./initall.ps1 [-help] <agency-name> [<args>]

        Giving no agency names will set up all the databases for all the available agencies
        Available agency names:
            exo
            stm
            Args:
                --no-download       Setup the database without redownloading the required files
    "
}

elseif ($args[0] -eq "stm"){
    execute_stm -Arg $args[1]
}

elseif ($args[0] -eq "exo"){
    execute_exo -Arg $args[1]
}

elseif ($args.Count -le 1){
    execute_stm -Arg $args[0]
    execute_exo -Arg $args[0]
}

else{
    echo "Syntax error!"
    exit 1
}
