package rocks.appconcept.javatools.xml;

import java.io.ByteArrayInputStream;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Base64;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author yanimetaxas
 */
public class XmlSecurityTest extends TestCase {

  public void testValidateSignature() throws Exception {
  }

  public void testDecryptElement() throws Exception {

    String responseXml = "PHNhbWxwOlJlc3BvbnNlIElEPSJfNmQzYjk4OTctOTIxZS00YzVmLWFmMjQtMWQ1MDIxOTk5NDc1IiBJblJlc3BvbnNlVG89IjY1MWIwM2VhLTNmMWQtNDM0MC04YjRmLWEyNjE5NTY0MmExZCIgVmVyc2lvbj0iMi4wIiBJc3N1ZUluc3RhbnQ9IjIwMTQtMTAtMTZUMTk6MzE6MjQuMzg0WiIgRGVzdGluYXRpb249Imh0dHA6Ly9sb2NhbGhvc3Q6ODA4MC90Y3NpdGUvYWNzIiB4bWxuczpzYW1scD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOnByb3RvY29sIj48c2FtbDpJc3N1ZXIgeG1sbnM6c2FtbD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiI+dXJuOmNvbXBvbmVudHNwYWNlOkV4YW1wbGVJZGVudGl0eVByb3ZpZGVyPC9zYW1sOklzc3Vlcj48U2lnbmF0dXJlIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjIj48U2lnbmVkSW5mbz48Q2Fub25pY2FsaXphdGlvbk1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMTAveG1sLWV4Yy1jMTRuIyIgLz48U2lnbmF0dXJlTWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnI3JzYS1zaGExIiAvPjxSZWZlcmVuY2UgVVJJPSIjXzZkM2I5ODk3LTkyMWUtNGM1Zi1hZjI0LTFkNTAyMTk5OTQ3NSI+PFRyYW5zZm9ybXM+PFRyYW5zZm9ybSBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyNlbnZlbG9wZWQtc2lnbmF0dXJlIiAvPjxUcmFuc2Zvcm0gQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzEwL3htbC1leGMtYzE0biMiPjxJbmNsdXNpdmVOYW1lc3BhY2VzIFByZWZpeExpc3Q9IiNkZWZhdWx0IHNhbWxwIHNhbWwgZHMgeHMgeHNpIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIiAvPjwvVHJhbnNmb3JtPjwvVHJhbnNmb3Jtcz48RGlnZXN0TWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnI3NoYTEiIC8+PERpZ2VzdFZhbHVlPjFFeDBFbzRDNlVGWHkxdjA3aVdXd1A2Nkk5RT08L0RpZ2VzdFZhbHVlPjwvUmVmZXJlbmNlPjwvU2lnbmVkSW5mbz48U2lnbmF0dXJlVmFsdWU+SFJlOCtOMGk0U2tzb0hnV1lncmh0N0NUVXJGR203WWVnRzZrUm9BcnU4VU5GNUE0RXJDZFYxMDRFYmZ4VUFTaitubldDL2R2c3Q2MktmWDRvS1R0bTc2bEtoa05wb2xnenBZblozckdoZjh5ekRqSDJwcmhNMkZWRVUyRVhKUVM1MTBkNWxGUTNoazN1Y2FvcmozZ2w1LzZPYTgvYjQvUmJLV0diWW4rTDlFY1ZEbmhCYVlQY25DRFprUHQwUU1TWjFSYzFuaDFqRnQ5WFhmeXFyOHdhM2M4WVQvUzF4WklMdUJNNGFmdmZtM3daWUtqVHJzd2hFSzlGR0JwalpMbGR0dUcwSnRjNzQvcXBBVjlSM1FsUGhjM2RzVDNyWWJOWU0rdEo5TnBnZE1nMUhCRng4WEZiMlFqaW9YWGtyZExxbm1XcmR3alM4SmxjK05Vd3Q0QW5nPT08L1NpZ25hdHVyZVZhbHVlPjxLZXlJbmZvPjxYNTA5RGF0YT48WDUwOUNlcnRpZmljYXRlPk1JSURBVENDQWVtZ0F3SUJBZ0lRZFBEci9pSTFqYmhETVRqNVZZeWErVEFOQmdrcWhraUc5dzBCQVFzRkFEQVdNUlF3RWdZRFZRUURFd3QzZDNjdWFXUndMbU52YlRBZUZ3MHhNekV4TWpJd09ESXdOVEphRncwME9URXlNekV4TkRBd01EQmFNQll4RkRBU0JnTlZCQU1UQzNkM2R5NXBaSEF1WTI5dE1JSUJJakFOQmdrcWhraUc5dzBCQVFFRkFBT0NBUThBTUlJQkNnS0NBUUVBaTBYSlJMRHJjYlN5cVVkOFhHNEJneE9iUU1ZTEFrRU5sbUpPc0FFcGwxeE1hYlVpcTFYNHYwRmM4WmFDcFVFM2ZGR0VOTUVXZ0JqblFVVUUwV3RWVWg1SlBNc3Vrb2xmOXFsamJKa0NrdkhYSDNPNFVlbjd2QTJvTlFXdDRiSzk2U3BYQURwWktGdnBrNEQ3YnRLT2dVL05hbWppcXdISTRmSThrRkpLd0tCSmNoUlBVUWRDNGxqUlJtR0lyU25wWSt0MjUvZDNLR1h3YmU5WjJNR0d5Mmh5QTB0Z09XdWNoSUsrMXZBS0tCVWg5bkRFWGZyODAreFc2ODB3NVRxSHlEY3FiV3ZRc1hYaEgweVpMZklOS05TNi9Jb2pIUHNCeTd0ZjM2Q2s5SDVQdysxUFB1Nk56QkZTejVaa0M4S3pyUzZ2dVpYYy9JbVlybmhlTVFzcXFRSURBUUFCbzBzd1NUQkhCZ05WSFFFRVFEQStnQkQ0ZFk0TUNQRW1HNHN4WnJjbmk4dnRvUmd3RmpFVU1CSUdBMVVFQXhNTGQzZDNMbWxrY0M1amIyMkNFSFR3Ni80aU5ZMjRRekU0K1ZXTW12a3dEUVlKS29aSWh2Y05BUUVMQlFBRGdnRUJBQmhhazJhUjg0TUNkeVhPNEFLT1F2Wnlic0NNZGhScTJpMWkwV2hENC94ZTdSeTVoYUM2VGVYSXA4UTRjQzNNenNyRGFsNzR4SEk3MTRCVzBsb2FmcEhBc1hmZDlFdmtLVFZhSisxWnBlMTYrU3NUTDR1cFMxY0d5ZGlncXdVenNkcEdjazR3STFtb0o5NDc3Tys0NklmMmdGMjd1OUNkazdPbnhlLzVkd0xJeFdta1ZSZGJRSUg1R3NLVWVBak9kUlFteStYMU1YNkt5Um9hQ3dXR1l3eGk1U2ErciszQXREdkQ0QlgwRUpHS0ZaZWVNM0oveU1wWWgvNzVhTjBjRlFmREVkSjdDNU5FMHZvbmlkRTBRdElGdnNvV3RaVXR1cjJmaVc3eUJ4c2UzOFRQUXNpMnI2QTZjL1Rac1o1YnEzMXloM2dyM2tTTjYySDhpVktMUUxBPTwvWDUwOUNlcnRpZmljYXRlPjwvWDUwOURhdGE+PC9LZXlJbmZvPjwvU2lnbmF0dXJlPjxzYW1scDpTdGF0dXM+PHNhbWxwOlN0YXR1c0NvZGUgVmFsdWU9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpzdGF0dXM6U3VjY2VzcyIgLz48L3NhbWxwOlN0YXR1cz48c2FtbDpFbmNyeXB0ZWRBc3NlcnRpb24geG1sbnM6c2FtbD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiI+PEVuY3J5cHRlZERhdGEgVHlwZT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8wNC94bWxlbmMjRWxlbWVudCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMDQveG1sZW5jIyI+PEVuY3J5cHRpb25NZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGVuYyNhZXMxMjgtY2JjIiAvPjxLZXlJbmZvIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjIj48RW5jcnlwdGVkS2V5IHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGVuYyMiPjxFbmNyeXB0aW9uTWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8wNC94bWxlbmMjcnNhLTFfNSIgLz48S2V5SW5mbyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnIyI+PFg1MDlEYXRhPjxYNTA5Q2VydGlmaWNhdGU+TUlJREFUQ0NBZW1nQXdJQkFnSVFkUERyL2lJMWpiaERNVGo1Vll5YStUQU5CZ2txaGtpRzl3MEJBUXNGQURBV01SUXdFZ1lEVlFRREV3dDNkM2N1YVdSd0xtTnZiVEFlRncweE16RXhNakl3T0RJd05USmFGdzAwT1RFeU16RXhOREF3TURCYU1CWXhGREFTQmdOVkJBTVRDM2QzZHk1cFpIQXVZMjl0TUlJQklqQU5CZ2txaGtpRzl3MEJBUUVGQUFPQ0FROEFNSUlCQ2dLQ0FRRUFpMFhKUkxEcmNiU3lxVWQ4WEc0Qmd4T2JRTVlMQWtFTmxtSk9zQUVwbDF4TWFiVWlxMVg0djBGYzhaYUNwVUUzZkZHRU5NRVdnQmpuUVVVRTBXdFZVaDVKUE1zdWtvbGY5cWxqYkprQ2t2SFhIM080VWVuN3ZBMm9OUVd0NGJLOTZTcFhBRHBaS0Z2cGs0RDdidEtPZ1UvTmFtamlxd0hJNGZJOGtGSkt3S0JKY2hSUFVRZEM0bGpSUm1HSXJTbnBZK3QyNS9kM0tHWHdiZTlaMk1HR3kyaHlBMHRnT1d1Y2hJSysxdkFLS0JVaDluREVYZnI4MCt4VzY4MHc1VHFIeURjcWJXdlFzWFhoSDB5WkxmSU5LTlM2L0lvakhQc0J5N3RmMzZDazlINVB3KzFQUHU2TnpCRlN6NVprQzhLenJTNnZ1WlhjL0ltWXJuaGVNUXNxcVFJREFRQUJvMHN3U1RCSEJnTlZIUUVFUURBK2dCRDRkWTRNQ1BFbUc0c3hacmNuaTh2dG9SZ3dGakVVTUJJR0ExVUVBeE1MZDNkM0xtbGtjQzVqYjIyQ0VIVHc2LzRpTlkyNFF6RTQrVldNbXZrd0RRWUpLb1pJaHZjTkFRRUxCUUFEZ2dFQkFCaGFrMmFSODRNQ2R5WE80QUtPUXZaeWJzQ01kaFJxMmkxaTBXaEQ0L3hlN1J5NWhhQzZUZVhJcDhRNGNDM016c3JEYWw3NHhISTcxNEJXMGxvYWZwSEFzWGZkOUV2a0tUVmFKKzFacGUxNitTc1RMNHVwUzFjR3lkaWdxd1V6c2RwR2NrNHdJMW1vSjk0NzdPKzQ2SWYyZ0YyN3U5Q2RrN09ueGUvNWR3TEl4V21rVlJkYlFJSDVHc0tVZUFqT2RSUW15K1gxTVg2S3lSb2FDd1dHWXd4aTVTYStyKzNBdER2RDRCWDBFSkdLRlplZU0zSi95TXBZaC83NWFOMGNGUWZERWRKN0M1TkUwdm9uaWRFMFF0SUZ2c29XdFpVdHVyMmZpVzd5QnhzZTM4VFBRc2kycjZBNmMvVFpzWjVicTMxeWgzZ3Iza1NONjJIOGlWS0xRTEE9PC9YNTA5Q2VydGlmaWNhdGU+PC9YNTA5RGF0YT48L0tleUluZm8+PENpcGhlckRhdGE+PENpcGhlclZhbHVlPlZIRzBqVEhrN3F4UUk3Nzk1ZTV1Ky9xTVcvUHF0SXFkc2VueE1sL3BJSVQxTmtKMU90ZHlLOG9EMHZKN2d2TFZva3pmcEJOcFBRdkNhdytLZ1ZYSkpNNy9OaDFmREtiSTFCQlFYdzJmZzN5Y1BlN0VycEhRZDJVRWsxR05DRlg5SjVVTkVLV2V4SVZYdTFnTlRlSUYwaHUxMXdQQURoZytRMzVwOHo4TitkazMzdnEyeHc2eXFJUGVlSkNieW9YMmpLZVFka280Z0F5ZENwaStCREhRVWlqTG9idHR1QVd1YW16QkV5NWQ1ZWFhU3Y5cTR0V0lCR1pUTE5SMEZJWXpkUHZwSUdBU3Azc2w1OXdreTA2TUxRSk41TFg1Q3M0TDJOdk5UMFlSWlY3eTBDWFNRZXRGSGQ1S1lFaFgvcFJBSGl5R0pnYUFkeGkyUzJNSEZrWk1sdz09PC9DaXBoZXJWYWx1ZT48L0NpcGhlckRhdGE+PC9FbmNyeXB0ZWRLZXk+PC9LZXlJbmZvPjxDaXBoZXJEYXRhPjxDaXBoZXJWYWx1ZT5DaWVLaTR2eGw2ejJMRUQyQkUyS3RUOVJWVjNkQ3pURGdWd1VIaFdBSndRVjFiREJseDBYSFBUREF5NEpHT1ZiN1RGc2xhcldEdnBCbGpPUTBhaG9vVzlzdTA1YUdxaUtSUWNzMndpeGgrbHU1Z2lCYkg3Z0syenM2bENCUjBOV0hsMitIOVhBb2NEeWdhdlNKVlpzb1B3dTMwNkNOUHFpcG9YVzg1WkltOUlHYm0rTDIyeTh1dFJkTy9ESEV5T1VwUThIMzF1OUt0cDl0Z0RIdEExZmdLdStHcHJTTXA5QkcwdVBzR0hReTU5TUtqVldnbDlCQ3NFT2xZdm00QlNOYWhSajR1bHN4d1ZDRjhzL1dxWkhuRDR3Q0lyVmFOS3JsOVRDNm12dU1WR3MwdExZRWsyd2dZa001djJmWVA2MUwrdFlmbmZWRXdMN3FDVGt4YXZ6enlHWDUyU0c1Y3I1bE1LZjBUbzNuc0lyaTdyNVVDNitmVEpocmhOMkZGaDBqaFZCbmtTcTNvbHc3MDVsM2NsdjREYjVVMS9yTXp4R0xtaS9QZ3FxMUFGY0Z5NUFWNFBKK0pCbW1Kd0NTMzNlTlpWRDAyazh1Sy9Lb3ljd0ozYlFaUE54K1NvcHNCSzNxQXVzTGlQTFZObmgrMnJaNkxiMVlqUHhUbnM4V0h5c211eUU1aWNXbkhaS0kvQ2wvNFRyM2I1ZVdPMXUwWENydkoydmxUcE05MUZFL3c0MkhsYU5UWjB1RXdVanlmYU9kdnQ1TmI3SnAxM1pmV0VyeWcwUmZRY0xwb2dUQ0VPeG9SMExKUXFRTjIyK1BpSWc1REV5M2grektoUUZnMDcxQXVCVm8zRVdZdU5RMzM0eHRSSkxmQU1nWnV3ak1rU2pYYkNnSzJubVA3MDUwZFk2UVdOV21kME5BZjZqYmRDbVM3aHNvTldxb2ZWd3JDME5uSTJ6TEZMV05STVJkVnJxdk9lSFFMNEVMTG9oZUlEejBVTWwvUEp2cGVRMjk3M2VyeTkrY2NBdUxyMHgwMDN0SDRrS1RpRkNrb2gzY0gzZW9qRTA4dk9PSHIxM0d4czJiNythK3d6aTZrY3Z5cW5uQ0lIdGFoUytmNTRYNG13SWVJbi9MK0FQU3F4eUpYZ2wzVnV2bjhxZFA0UjUxY1RMZ0luYU5oaXZ2MXNvUlhpWXIxdHV0QVNXdGU3VlBsRGxNcS80cDdkVHVWTnUwalFXNi9jRzVqQWF3ZklFcHdzMm9aSllGM0RtQ3FWR0s1SGM4T3BvVU5ZUi9ib1BqalZQdHc1UGtJMlRhV3kzRmgwQy9jN0FGOVJrZGtFMzZZUWduOVYyV2JNeWp4VENjSDAwN1RZcHhxL0dLelpKc0JHaVlaQWlaZWhGRmEvKzd3MjJnQ04rdnZYWmZZQUtrbWl2dHBmT0JXQ050Z3dwRzNTY0JQYk1zTUtBV21XTUhPcGMxWEhxQkh5WDR6WmNRZDNCbG42MXg0RVQ1V3FCUGdkRU4rZkpVSEMxVXVuYWlQampNVXl0eEtuRmJZT05mYStvUzRaaXFZTUJSV29wVW91bEd5cXUrVFBJOWhFUWdDSThxS3hkWTR0QWRQaHNScVVzVk1vcG5MdVkvL1NCYTZoWS81SUFsRnRRUDROK0hCcGdDcTBuWHU0RHpHQjcrS0NQOEdJRlYvVGNweWs0UzNoa2ZvVU12T1orMk9wUUptSklpWldZbFdieHAvbkVuTjJzWWU3aTgyOE5VcG1qV0dtSjZHK3l1Mk9Ia1FCYW5QTnhzR1pDMUtsckoybHlxMm14RWRHdUR4M0t5dkxnVnpVZkNyVW1xUXh6QmFXYitTRi9PK1UxRm9weDUxc1kvUlZzVmV2cTc5Ymdsb05ZMFUwMFdIcStzOUovd2syZ1phY3gyc2NRaDFJN0J4UGtERGdhY2xLT3dRV1piamlyanN6eDlCQ3B4QXZrQUpQa3J3ZFNRb0hYUGcyYUo0Z243eDNMYi8zRlk2YTA4VW42NU1MMHlxbHRhb0xLYktMTVBBQjFpOGlncE9nVDZHSjNtV0dHd0JSc0VpRTNrb2htdUVUTjdoaDhLT3k1QWxxUE1zNDdpNXhGeElOZk5LY3VCbTBGTC9KWlF2L0cxYW5yeGxxQjRtT2ZaRndpVWtUYkxhdDJZL3JYdmZ5YkFyQXNWMFBDK0xKN253djlGZmQzS3ZnMDVhSUVITDViVlRJUm03TDVpRzh3SEVYdGVaYTlQNjlrQ2tEMVJKamxDcU56VEtXSW5wM2Zwb3NoMGhjVERtSTNoWFdoa3h4cFhLNkdYdU9xZXdCVXpFVVdnU0JoRklFajN1SVhUMkl5NVdQamVOMlhEVkNIVXFxV1JzbzFRcGNpZExpL0tiNzl4dzF1eWxHYW16Z05qaEUrSE1nVXBUdmt2UllaeW9wdDd5bU4zaGZtTWVuNk1IWDUrQSsrSThvUWNNdml4a1hnY2hGWTFHSER6Z1BIZDIyd08yYlppUGg0elh6MDhOL2RPbHBtM2FUV1dMdkF0MmVQd2Y4VnMxWW1OZ2laaGZkcExEdEd1T3JxMTh0b0hQU0NUb01sSkpoZE52S0M0alJVS1hudzVFUm5PTGxwSUl0Tnc0VWtER3pucDZwOHVORGVTcmhzNU5lYmtJVlJWZk5GeExSRitPMmx6Um5xRjNFTkRnTEdGQUMxU2ZZTHlQZGhHUFQwc1NOUXhMSjFOeGFtUURDSHZjYmtXaTdIQnhvMUZ5aERHZmFpaTJ0TjE1V2M3c3RCVEpiSDdhbklhakd1Qk4wTUppZGpuaUlOOHlxbWpSeG43bVBDVEMxWDRRdW4yUk1icUhiM0twM1k5a0pOYVdKNkcrb2VGeTA3QU9nZVBQYU5VN3VpbDFZd0FCT2UzenNjS1NESnFsakYyWCtvcHBpaDdnNDJ0UXAyWUFWNHV6bTFweUE2K2EzRjd5WmtRakI5c0kydmpBc3YrK3owbnozUW9CSnpabW9reDZvdEZMQTU2RHBFME1pa0h6Y2MzMllsOWNNbDN3MnhBekhWVk14NUhsc0JkOGFHVmg0N1gvL1pzdU42VnRXeVZrcWRaeFl6YW5CWDJmdnZUSXpDWWlsK0tuSEFRNEh4TEFKSEZMSUc0ckR3YzBETEdodVRUWVZDOTVuZVpkNVEzV2ZwaVpacmx1T3cvaVltbVNXUGxORVZJTHViVy9nbnRyWXZRRWF2UHNscWdrRzJlb0kzMDBkYmZ2QzVkVHJWdi85Nm1zTU01eEZua0xEQ2NETkVEOHdMK2hUOTl0em5oUHRLRng5c0lneGhkRlNrbnV1dG1Edjk4cDQ0YWY5eWlPdzc5WEhialNHYVVkZWJjVmwzRElMREJIcFpVNlF1emZjTHJRcFdSakF3RVRMbzN5VXY1ZG0vSzluMjJuSW1uMjZ5T1YxeFdFZUlKY0FpMGhXYmkyQWRzeWpaZFNmU2dQM3FkdmloR05VeVhKa3hVVFF3N0dPS1hJalFvNGxiZ0JzaGlPUnRweTkxUllHYVMrWWdFeHA4bEV1VFNHVHlmRUNSSENhUFNGcEZuZkFxZUFUOVYrK1VGcmxKWUE4UW9VemhGWk4ycnhncjFaRlNEN2lTYXQ5cm12LzlkWmpIcmFhZWVHRW9GdjdDbmpYK2ZWSjQxREVTZnlZVE5RbFpkY3dLdkpmSW5JSC94Mm5sT0hUZmRYU1MySGE2U1ZxendobUlPcG9wdndmd08xeEpHZ3ovcmtRUC9XRm1uQVZNZ0VDbVJhWGg0dE11VTlEeTFLUjIya3NOQStQSDEvV3U1bW5MMVNPQ2RFT0FjaDZnbkowdCsyUEo0R2Z1M0tSYlpva2swbDhzZ0hONWtnTlhPQTJucWlCVkhjdmxXencyZzUzbTM4TktJaW54T1ZTLzRqQ01TYzZPb0N4S01PV2FaT01YS1B1ZWJrcWNoV1BmVWdPY0FnQTJ1V1VYVHVQeldzVHcrQkErZjBIeW9kVXBZbG9tQSszQW40bk1ZaW1xZTlYUjBTZ3VsU3ZuOWNVS0RtNEViK000UWVDZzQzR2hOZzdEWWZnbHh2dXJJUmlnQWVha3hEVUw2L0RiSklNajJkbDNwYWxzWVdXSWk5YUJnMnlJWXM1WVR1Qkc0eGdVQTZmaHg4Uk1tUkV6NmJmNWx6UVlWd1BZZjJ2TlMzYStTdXVSVzYxRzBuZDJBS0FucmhyQ2kzRjhvVWpIb1B6ckpUTUFVUE8xblZvQzhOME05Q2krcjc2QTNVVVRRSnc5TzM3ZFdIeXR4N2JoQmZaMVNqWGtUMlA1d2EvaU0rNHJtOHZMYWovbHdMS2ZvMkZpbGFrbjJHQU9MMkFIMVFIMS9YUmVtNjVFQ0Urb3hGMWNpaVNKTFJJeTdPNTZnOUxtNFQ3dkF3KzAycWtMVllzeFNFL0xRYnpzZHBVUnlrSHJvQUNnKzJ4eDFUcCsxbGgwdEgyMGtkdWVQK1JGamxBQWNWeE9CdzlRNXVCNmt2Z0tNdUQ1bUxBM0ZwUXd0SC84Y29SaklLNnFac1Y1Y2NQVU82Z0hDU0Iyd2tOZk5QV3RsMmRMQS93OWRpa3UwSDVodkcySGMxOG9rcUl3cy9PYWZsSUFqSksydXJkZnU5enMyTkVTbWEyYjVpTTZGcWRocTNCR1JJUlRvZHNEUWNYcXVPQmh3WkRLbWtYL1N4ZVhRNFFrOEJqK3VyTEVtek9aUnFsbTJ6bXVob1JkQmYvOVpISW1EZHJJVlFlTGFYeEtEekxjUmhwSnFwajh0MW0zc2NxK0ZqOVVINXVQWXlIT0xHSGVZMVBRc2xZZGQyWkw4VzA1dEZlVUYzcFRDZXhDWmRiazlGaXd3ZmFwZTZldXYvMi9JbmdkU3lkSUZ4UlYrQjRKckpoeERuSXM1bFpnOHN1eXRxL3pLeEphMEhkZE9GQjFvcDhURXh4WnBhaWVLaGVQWEJUc1J6MjI3eHVmZm1aa0pHd0V3YnlUalRUcjBZRE5GWk5vTnZac3lJSW9yVTJNNC9CSDZkVk5XdDllQ3N5cXBjNERwQ0JGeXNkdEZxNXlXdEV2QTZDN0NaTWtYTVZQTkJtUVJERmYwMHFmM2FyV2FmME5iVkIyS0tXWUQ2azBJNzVqTkxzWEFqQVVLVWNWYnd0RmtyWHIwbXJBQUxiVElzM0IxdVU2aVVNM1pQYlVvcVA1MlFkMFEzeGJjbU1GMDdLYzI2SVFCTmwwWENlWnFDMHc5cHBCc1c5MVBZdkxwV0RZRjJGeWp0cjMwZURTSU83ZmpCRmhiamlYeDZFSVFnSzJXRE5UcWIyNkw1WDdQeGE4bTltdlVVWUx3ZHQ5aXZ4czdIUWRZWGt0SGhlU2c4UzZzS1B5ZzlpaFM4Q2dBWTZXcVgvczE3WGxTOTFBRjBqb0pIQmlmYTBxSGJDaGpVTkFtZkl2T1ZxQkYrR1VReGtIMTBRYkhLMVBycXdETmVjTm90eW9RR2FhN3dQMnBkckgzeXRxb2JzSWw5ZTViL0ZqMENYT3BTMmcwZ3RzdCtvMFE1SnpaZ1JPMkxmQUQyQnIwZi9iZFZqMVZjU2tQeWJJdnVzT0pZU1hub1hQS3g1UnVJdHhjbmhpaHJJdUxnU1p0QXFBOHlFc1JlWDZJTC82MG54a2pDTVpmMVJFaDBENEx3WEgyK1pSeGhzRXFtSDU3M2ZGRE1JNnFtTWVSbHR5NzA2T1ZsR3BZdzdxOUtNOUp6elVlNVUvNGRiT1EyT2FVSWJqcTJWN0U4SCtwVXhoK1h0b3Z6U0Z3UHN0Z1k5OE5FNTN5bDAyM0ZKUG1JYlRnRHZDM04wV3crT3EybE5yRitSc1hKSG5oc1p4ZFJSUStmWDY1VmpiejRlWVF6Qi9mZXNpNE5IYVhhL3EwbjVZdVEwcU1hdkVtclhmRGplL2NPN1lLM28xUEpzZ0hoVGtZd1JtZGkrM1ViWGtwdTFXbWp2RWFJYU4yNXJYSVhuQ2FpOExzUE12L0M2YmxsUmM3VU9MM0w2UlU0T1dqVExFUjBvZ3NhbDY3ZHczNW9QMTdHY2dlK2s4enpQOGo2THAvTEg4ekVYSzYvQkN0TS84d21FNEdHSmZEa0U1TngwVGxPcWlBT1g0Q1d6WFZMbGZ2WDJEWVlWODVFRzdZQjV0Z1F0ZlIwQjgyMWswU2g2U2FSaEt5U1BKRE9PRDJpZzR6N0NVTWMwMWo0K1dMVDUzeEdBMStlWE1uWnFPNUFtcjlqdXJ6MlA4TFRwOUIycERIQnUrM2JEWU1QK25MSVp1aWhFQ0tOVWNEMytZNklGSDJHc2g2bWVFYTBXN0JMK0NqWFBNNHg4MlNQWWRwVlBjTStrN0ZxL0FrbnRvL3d1SThlaW10ZGROdUh5Yjc2Q0M2NFI0bXVmUEY0N2ZCY0RnQVlnbUFwYmZtTHpYUzVWK3pGVU45OGJmSG9HN0IydStxRkYxRm9XanZlT3VhaC9nNHVEckNwSU5hN0hSOTNjSWdkZU1wRU5mV1VWbGRTV0FaUjlrcWcyeEdzR1kwZ2ZKTzFUQTJzcmdtK3NWUURwVVQxU2V1UDcxdTNsK21aNkxTODZuTUpnV0lxSHRyMFM3dXgzQWlRZlJMZFNmSk12Q3owWHIzTEJWRURDR3d4OG9DNzR4R1kwQ2M3ZDVKc0VFL2tYNWVlNWNscE42aTVZME5CZ013V0xIMTNVTHVJMEdxZy9LbXVxM1dNS2JNSUpNa0RiNEl3clJNMlNRUTN4UFBUZDIzdVVhSi9jVFIwbEN5NU40NGZYNEhiMGwxRXk4UG8yR3pXNS9Rc1AvZEw4K3gxTjJhUHpHeTQzbDQrTEZRSXVaK3EwSS9Qa1hFMDU4OHRWQ2t6SHJTRktCMHBMWG1sZ0Q8L0NpcGhlclZhbHVlPjwvQ2lwaGVyRGF0YT48L0VuY3J5cHRlZERhdGE+PC9zYW1sOkVuY3J5cHRlZEFzc2VydGlvbj48L3NhbWxwOlJlc3BvbnNlPg==";
    String privateKey = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCLRclEsOtxtLKpR3xcbgGDE5tAxgsCQQ2WYk6wASmXXExptSKrVfi/QVzxloKlQTd8UYQ0wRaAGOdBRQTRa1VSHkk8yy6SiV/2qWNsmQKS8dcfc7hR6fu8Dag1Ba3hsr3pKlcAOlkoW+mTgPtu0o6BT81qaOKrAcjh8jyQUkrAoElyFE9RB0LiWNFGYYitKelj63bn93coZfBt71nYwYbLaHIDS2A5a5yEgr7W8AooFSH2cMRd+vzT7FbrzTDlOofINypta9CxdeEfTJkt8g0o1Lr8iiMc+wHLu1/foKT0fk/D7U8+7o3MEVLPlmQLwrOtLq+5ldz8iZiueF4xCyqpAgMBAAECggEAIe+KQFufsyAlnIvXqKoBYlAHt3vKOO1jAy7HH6nY2ZHa6LMDVJ5pMykZHaDgCqZef8NZJIWcFvB6gPPxXZn6uzy3+cFEgt5IbNEPcenCr82P7pEC66rI9SSaxPiyStmD3um7M0oEaYoj2xZFrdZOdQHd+ry95o9TxIKC8WOr5U2NICj5xkqTI52LH1FjN98I/VYRoLZeBXUKsiB+7tAhA1EVAplVlnAu+Mlyn2tN5cW3ktO8IvRjdkNCfN1bZVCjf+suV8BmS9AfLe4+f9fu1wdhso0erQN9JwtdhQNeksQH9Z9C8xCsTsNu5AvtvQO7rjdPaB0xu6ro1nOOlImJhQKBgQC1cbVvvOTbwAFUykc+EmKdvVRydFeNqAcOecRqxNfpJz1MPs02yaXChV9LwCs7C6jjkLsOh8V/jkkIP0YOiYTQUQNJORFfhiFgssDHhrLdUcky/LXngmzyY6HENO+x+Gj4zrkTIPFT3Nly8k8hddNUOwdefIOhAQlpNPrSSxCo+wKBgQDEgAIeYlH9/9kV53yRuvLi3aO/IwleYwAvSO/XquBFwHpV8i0CeAAeLV2GBhTlJwqkHyb0THpJuMLssKqXOtyUNFOBOkXbf/emXmJKp4Mt/z9U/VTEJ3cd+GdA5JCrUt2m9VhDdEIjSqXF3s3gWSdbsgmaD5TbmxyXp5bkHaLxqwKBgQCw6A74xPl2cHxbyWUw1ggkt+ZQObLrwLowmPSzDP1erA2N+2VmV7EwOY2yc8kypqmiRwi6oBIlzfrS+a46mkZOPc6wY93WfxaoC2D+qQxX0VgUSGSrNnU214FvphVWNldSz2uPhP0GwTuaYTpZ62GCrJMAwvIr1rDwTOGIi/vQZQKBgCiNVmKY3dzycQWhOUcF8tw463MlsuhEvCQGBGFYxWH/v/9+tCU/SJ7xs8xV8vf7n6mROEPQiKgwNiIMr5f4MM9xIQ6ZTRNojF5MOjK38U2L0LEketrkXiPUQNLWmLiSqGWtWmLJWmDYJe/49DnpjELTMkIWFx1fHH57upsnNppzAoGBAKK7rnrZpq31xFDIqLzR49qA3osG30vffE2bAwChPcAv3FvwGWKKu5SxEMdQeEKQWo/SgtiOE05wNyPhO0qWyqOPZlEzQEA9uoknMzKkTMgN5UNZRGiUIjqmmEte0yh1gEc33dJoKn4YjovhaM8dTzPZiUpcQeLk37MU+tcS+DUQ";
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    DocumentBuilder builder = dbf.newDocumentBuilder();
    Document doc = builder.parse(new ByteArrayInputStream(Base64.getDecoder().decode(responseXml)));
    NodeList assertion = doc
        .getElementsByTagNameNS("urn:oasis:names:tc:SAML:2.0:assertion", "Assertion");
    NodeList encryptedAssertion = doc
        .getElementsByTagNameNS("urn:oasis:names:tc:SAML:2.0:assertion", "EncryptedAssertion");

    assertEquals(0, assertion.getLength());
    assertEquals(1, encryptedAssertion.getLength());

    XmlSecurity.decryptElement((Element) encryptedAssertion.item(0), privateKey);

    assertEquals(1, assertion.getLength());

  }

  public void testParseRSAKey() throws Exception {
    try {
      XmlSecurity.parseCertificate("abc");
      fail();
    } catch (Exception ignored) {
    }
    PrivateKey privateKey = XmlSecurity.parseRSAKey(
        "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCLRclEsOtxtLKpR3xcbgGDE5tAxgsCQQ2WYk6wASmXXExptSKrVfi/QVzxloKlQTd8UYQ0wRaAGOdBRQTRa1VSHkk8yy6SiV/2qWNsmQKS8dcfc7hR6fu8Dag1Ba3hsr3pKlcAOlkoW+mTgPtu0o6BT81qaOKrAcjh8jyQUkrAoElyFE9RB0LiWNFGYYitKelj63bn93coZfBt71nYwYbLaHIDS2A5a5yEgr7W8AooFSH2cMRd+vzT7FbrzTDlOofINypta9CxdeEfTJkt8g0o1Lr8iiMc+wHLu1/foKT0fk/D7U8+7o3MEVLPlmQLwrOtLq+5ldz8iZiueF4xCyqpAgMBAAECggEAIe+KQFufsyAlnIvXqKoBYlAHt3vKOO1jAy7HH6nY2ZHa6LMDVJ5pMykZHaDgCqZef8NZJIWcFvB6gPPxXZn6uzy3+cFEgt5IbNEPcenCr82P7pEC66rI9SSaxPiyStmD3um7M0oEaYoj2xZFrdZOdQHd+ry95o9TxIKC8WOr5U2NICj5xkqTI52LH1FjN98I/VYRoLZeBXUKsiB+7tAhA1EVAplVlnAu+Mlyn2tN5cW3ktO8IvRjdkNCfN1bZVCjf+suV8BmS9AfLe4+f9fu1wdhso0erQN9JwtdhQNeksQH9Z9C8xCsTsNu5AvtvQO7rjdPaB0xu6ro1nOOlImJhQKBgQC1cbVvvOTbwAFUykc+EmKdvVRydFeNqAcOecRqxNfpJz1MPs02yaXChV9LwCs7C6jjkLsOh8V/jkkIP0YOiYTQUQNJORFfhiFgssDHhrLdUcky/LXngmzyY6HENO+x+Gj4zrkTIPFT3Nly8k8hddNUOwdefIOhAQlpNPrSSxCo+wKBgQDEgAIeYlH9/9kV53yRuvLi3aO/IwleYwAvSO/XquBFwHpV8i0CeAAeLV2GBhTlJwqkHyb0THpJuMLssKqXOtyUNFOBOkXbf/emXmJKp4Mt/z9U/VTEJ3cd+GdA5JCrUt2m9VhDdEIjSqXF3s3gWSdbsgmaD5TbmxyXp5bkHaLxqwKBgQCw6A74xPl2cHxbyWUw1ggkt+ZQObLrwLowmPSzDP1erA2N+2VmV7EwOY2yc8kypqmiRwi6oBIlzfrS+a46mkZOPc6wY93WfxaoC2D+qQxX0VgUSGSrNnU214FvphVWNldSz2uPhP0GwTuaYTpZ62GCrJMAwvIr1rDwTOGIi/vQZQKBgCiNVmKY3dzycQWhOUcF8tw463MlsuhEvCQGBGFYxWH/v/9+tCU/SJ7xs8xV8vf7n6mROEPQiKgwNiIMr5f4MM9xIQ6ZTRNojF5MOjK38U2L0LEketrkXiPUQNLWmLiSqGWtWmLJWmDYJe/49DnpjELTMkIWFx1fHH57upsnNppzAoGBAKK7rnrZpq31xFDIqLzR49qA3osG30vffE2bAwChPcAv3FvwGWKKu5SxEMdQeEKQWo/SgtiOE05wNyPhO0qWyqOPZlEzQEA9uoknMzKkTMgN5UNZRGiUIjqmmEte0yh1gEc33dJoKn4YjovhaM8dTzPZiUpcQeLk37MU+tcS+DUQ");
    assertEquals("RSA", privateKey.getAlgorithm());
  }

  public void testParseCertificate() throws Exception {

    Certificate certificate = XmlSecurity.parseCertificate("-----BEGIN CERTIFICATE-----\n" +
        "MIIGWDCCBECgAwIBAgIBAjANBgkqhkiG9w0BAQUFADCBpjELMAkGA1UEBhMCVVMx\n" +
        "DzANBgNVBAgTBm9yZWdvbjEpMCcGA1UEChMgRGFpbWxlciBUcnVja3MgTm9ydGgg\n" +
        "QW1lcmljYSBMTEMxCzAJBgNVBAsTAklUMSkwJwYDVQQDEyBEYWltbGVyIEludGVy\n" +
        "bWVkaWF0ZSBDZXJ0aWZpY2F0ZTEjMCEGCSqGSIb3DQEJARYUaWFtZ3JvdXBAZGFp\n" +
        "bWxlci5jb20wHhcNMTEwOTI5MDgxMDU1WhcNMjEwOTI2MDgxMDU1WjCBujELMAkG\n" +
        "A1UEBhMCVVMxDzANBgNVBAgTBm9yZWdvbjERMA8GA1UEBxMIcG9ydGxhbmQxKTAn\n" +
        "BgNVBAoTIERhaW1sZXIgVHJ1Y2tzIE5vcnRoIEFtZXJpY2EgTExDMQswCQYDVQQL\n" +
        "EwJJVDEqMCgGA1UEAxMhaWRwLWR0bmEtdGVzdC5xYS5mcmVpZ2h0bGluZXIuY29t\n" +
        "MSMwIQYJKoZIhvcNAQkBFhRpYW1ncm91cEBkYWltbGVyLmNvbTCCAiIwDQYJKoZI\n" +
        "hvcNAQEBBQADggIPADCCAgoCggIBAJoYG2EYY6XjsxdGnBX7q3XpgAmgUCg1wQLp\n" +
        "PzK/TuTP0GJW1Ll3b2FKnO3K2pI/56i1wXTN9/KSa+3CCDtAVZqcR0PMO+w0QEPN\n" +
        "i7FMsaWj0XbbQ3usS5rmZHydSHXlOMQt43urVDdZScnJ0LXSBMq0X6GS1/z7bw0O\n" +
        "L+7HuJ8HbybGUt/IGj1ov1cXgYyc9150Byb3+NSJucuDNW0ndW12KBwHi3FLvyg0\n" +
        "yeZQqKSb/NHlakRyoeuJYiCY5fNWlw7tUUkbWw/JX5Kd+Z6OIsx58ClDnR5b85Pq\n" +
        "NNeCMZW2VNH0yrZ39tRFX2z75OsAjPhcBsLBqbOTg6A9Uh5IXysXkuG6giHT4zDT\n" +
        "GpVqueKBg/MyG7fsyDqHO59JLaK1ygzOKCXPAzXCcgBagw/5A0EEPv9fdHukZBZ7\n" +
        "B/kgPH1/gszZ6mMXHog31UrTVliLWcYVyBlQuk8BL9ARn6IT6o9NTnM0RP5wZr5d\n" +
        "0Xi7NgyQwTFhHDL+9QT3fa38Nw3wgoRSsFUF0YNDXgs9R3jh+6d6g/gQnRSWt7eO\n" +
        "5Y9bxlM8ABNtMIILp1AYJD8fZ7Dp2OZLoDAqppDdyTKSTOrK2bVXhJNm/hGd3mOJ\n" +
        "NG0wlXuHXBTDPgC923fxEicw3N5E3YF7q2dErh92eQOeumhanfuHw8P3rLpmPpVa\n" +
        "ULoyjFgjAgMBAAGjezB5MAkGA1UdEwQCMAAwLAYJYIZIAYb4QgENBB8WHU9wZW5T\n" +
        "U0wgR2VuZXJhdGVkIENlcnRpZmljYXRlMB0GA1UdDgQWBBSY8hPDgo4PksgJGNt2\n" +
        "db8zFv6OrTAfBgNVHSMEGDAWgBSw1RelCkOFT/YW9o2504F/4uYSZjANBgkqhkiG\n" +
        "9w0BAQUFAAOCAgEAK8jahrwq7p763CNnOgvkXiUm6c4FXMe8zPHN4tfBPLFmEN3m\n" +
        "mXsxXYHXb1XLLuk59hR5ploqGS4b2lxaq0BqLM+qPGKfo+0iytu+YI50JCMH2fXI\n" +
        "szdlt4XsnM8IjgH8gYernIZm/nuMN+JoHF06tAZyVTwd01FIZc7Im7DdCS5RQ7qb\n" +
        "7bucVAVcm/tff4TFrDqQSPqL3dak0un+hziqNbpj1AJkY+vzAOqIUsNjw1NsEymZ\n" +
        "FD1uVfVBwglDMWgnLS8eY5PHjBdjgUPPvw9gZe6X3i0PFAl+BWVCbcMtb1MdZ8LT\n" +
        "RYFNkeGhPyUq7dunyxOF8kyKbHhwoxjuWV0fCacuaubT3Rh5drdpiDOeX3DonkQd\n" +
        "/mR4FqVRnaH13wa/D7Rk6Db6yjs/43j0bSSQTSXwn3psl+eL7adVA+2k+p8jOy4x\n" +
        "t3/8FUR4xGj3I5gLILKPiH0LfyZ9N5UmxMZKi9fVmp0lN5bko9ShiFf7oRqmDI0V\n" +
        "nVuXldW5StsEFU0SBiyTQ/L7TH9GK3s4PsAjMQ6hbpD4YGwwe8/hbKhk6rbOFf2v\n" +
        "3Aaws9Wgm7HLrBNo9bhyGtnNxEdng3vgLEpJitj6HgOQ7WwTy1zhGQjLVxBn+ifU\n" +
        "nZRO8T4c1U7jGHZDjvf+JmNTAt9E3ihwdXI3d32Mb9J8I46qNuOTTtesrbc=\n" +
        "-----END CERTIFICATE-----\n");

    assertEquals("X.509", certificate.getType());

    try {
      XmlSecurity.parseCertificate(null);
      fail();
    } catch (Exception ignored) {
    }
    try {
      XmlSecurity.parseCertificate("123");
      fail();
    } catch (Exception ignored) {
    }
  }

}