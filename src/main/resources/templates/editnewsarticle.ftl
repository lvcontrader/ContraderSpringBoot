<#-- @ftlvariable name="newsarticle" type="it.com.ibm.generali.capitalreporting.model.NewsArticle" -->
<#include "base.ftl">

<#macro page_body>
    <#include "topbar.ftl">
<!-- MAIN -->
<div class="row">
    <div class="small-12 medium-6 large-6">
        <h3>Edit News #<i class="generali">${newsarticle.id}</i></h3>
    </div>
</div>

<form data-abide novalidate id="editnewsform" method="post" action="">

    <div class="row">
        <div class="small-12 medium-6 large-6">
            <label>
                Title:
                <input id="title" name="title" placeholder="News title" type="text" value="${newsarticle.title}"
                       required>
                <input id="newsid" name="newsid" type="hidden" value="${newsarticle.id}">
                <span class="form-error">This field cannot be blank</span>
            </label>
        </div>
    </div>

    <div class="row">
        <div class="small-12 medium-6 large-6">
            <label>Body:
                <textarea id="body" name="body" rows="4"
                          placeholder="News body here"
                          required><#if newsarticle.body?has_content>${newsarticle.body}</#if></textarea>
                <span class="form-error">This field cannot be blank</span>
            </label>
        </div>
    </div>

    <div class="row">
        <div class="small-12 medium-6 large-6">
            <label>Link Title:</label>
            <input id="linkTitle" name="linkTitle" type="text" value="${newsarticle.linkTitle}"
                   placeholder="Add a link title" required/>
            <span class="form-error">This field cannot be blank</span>
        </div>
    </div>

    <div class="row">
        <div class="small-12 medium-6 large-6">
            <div class="row collapse">
                <label>Link URL:</label>
                <div class="small-9 columns">
                    <input id="linkUrl" name="linkUrl" type="text" value="${newsarticle.linkUrl}"
                           placeholder="Add a valid URL" required/>
                    <span class="form-error">This field cannot be blank</span>
                </div>
                <div class="small-3 columns">
                    <button class="button"><i class="fi-link"></i>&nbsp;</button>
                </div>
            </div>
        </div>
    </div>

    <div class="row">
        <div class="small-12 medium-6 large-6">
            <#if newsarticle.published>
                <input id="published" name="published" type="checkbox" checked="checked">
            <#else>
                <input id="published" name="published" type="checkbox">
            </#if>
            <label for="published">Published:</label>

        </div>
    </div>

    <div class="row columns">
        <div class="small-12 medium-6 large-6">
            <div class="button-group float-right">
                <a href="editnews" class="button secondary">Back</a>
                <input type="submit" class="button" value="Save">
            </div>
        </div>
    </div>

</form>

<!-- END OF MAIN -->
</#macro>

<#macro  before_end_scripts>
<script></script>
</#macro>

<@skeleton/>