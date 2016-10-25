@Grab(group = 'org.codehaus.groovy.modules.http-builder', module = 'http-builder', version = '0.7.2')
import groovyx.net.http.RESTClient
import groovyx.net.http.HttpResponseException
import org.apache.http.conn.HttpHostConnectException

/**
 * Created by shaybagants on 4/30/15.
 */

def query = 'items.find( \
    { \
    "$and" : [ \
        { "@layout.environment" : "DEV" }, \
        { \
        "$msp" : [ \
        {"property.key" : {"$eq":"layout.folderIntegrationRevision"}}, \
        {"property.value" : {"$nmatch" : "2015*"}} \
        ] \
        } \
    ] \
    } \
).include("path", "@layout.folderIntegrationRevision", "@layout.environment", "stat.downloads")'
def artifactoryURL = 'http://localhost:8088/artifactory/' // replace this with your Artifactory server
def restClient = new RESTClient(artifactoryURL)
restClient.setHeaders(['Authorization': 'Basic ' + "admin:password".getBytes('iso-8859-1').encodeBase64()]) //replace the 'admin:password' with your own credentials
def dryRun = true //set the value to false if you want the script to actually delete the artifacts

def itemsToDelete = getAqlQueryResult(restClient, query)
if (itemsToDelete != null && itemsToDelete.size() > 0) {
    delete(restClient, itemsToDelete, dryRun)
} else {
    println('Nothing to delete')
}

/**
 * Send the AQL to Artifactory and collect the response.
 */
public List getAqlQueryResult(RESTClient restClient, String query) {
    def response
    try {
        response = restClient.post(path: 'api/search/aql',
                body: query,
                requestContentType: 'text/plain'
        )
    } catch (Exception e) {
        println(e.message)
    }
    if (response != null && response.getData()) {
        def results = [];
        response.getData().results.each {
            results.add(constructPath(it))
        }
        return results;
    } else return null
}

/**
 * Construct the full path form the returned items.
 * If the path is '.' (file is on the root) we ignores it and construct the full path from the repo and the file name only
 */
public constructPath(groovy.json.internal.LazyMap item) {
    if (item.path.toString().equals(".")) {
        return item.repo + "/" + item.name
    }
    return item.repo + "/" + item.path + "/" + item.name
}

/**
 * Send DELETE request to Artifactory for each one of the returned items
 */
public delete(RESTClient restClient, List itemsToDelete, def dryRun) {
    dryMessage = (dryRun) ? "*** This is a dry run ***" : "";
    itemsToDelete.each {
        println("Trying to delete artifact: '$it'. $dryMessage")
        try {
            if (!dryRun) {
                restClient.delete(path: it)
            }
            println("Artifact '$it' has been successfully deleted. $dryMessage")
        } catch (HttpResponseException e) {
            println("Cannot delete artifact '$it': $e.message" +
                    ", $e.statusCode")
        } catch (HttpHostConnectException e) {
            println("Cannot delete artifact '$it': $e.message")
        }
    }
}
